package com.mymedia.nagasu.service;

import com.mymedia.nagasu.dto.ImageCollectionResponse;
import com.mymedia.nagasu.dto.ImageDetailsResponse;
import com.mymedia.nagasu.dto.ImageUpdateDto;
import com.mymedia.nagasu.dto.ImageUploadDto;
import com.mymedia.nagasu.dto.metadata.CollectionMetadata;
import com.mymedia.nagasu.repository.ImageRepository;
import com.mymedia.nagasu.utils.FileUtils;
import com.mymedia.nagasu.utils.SlugUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import static com.mymedia.nagasu.utils.FileUtils.getMetaData;
import static com.mymedia.nagasu.utils.FileUtils.saveMetaData;

@Service
public class ImageService {

    private final File imagesDir;
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    public ImageService(@Value("${storage.path}") String storagePath) {
        this.imagesDir = new File(storagePath, "images");
    }

    /**
     * Returns all image collections.
     */
    public List<ImageCollectionResponse> getCollections(String sort, String order, boolean includePrivate) {
        FileUtils.ensureExists(imagesDir);
        var list = ImageRepository
                .getCollectionDirs(imagesDir).stream()
                .map(folderName -> {
                    try {
                        var folder = new File(imagesDir, folderName);
                        var metadata = getMetaData(folder);

                        var itemIsPrivate = metadata != null && metadata.isPrivate();
                        if (includePrivate != itemIsPrivate) {
                            return null;
                        }

                        var thumbnailUrl = getThumbnailUrl(folderName, folder);

                        return new ImageCollectionResponse(
                                folderName,
                                folderName,
                                metadata != null ? metadata.title() : folderName,
                                metadata != null ? metadata.artist() : "Unknown",
                                metadata != null && metadata.tags() != null ? metadata.tags() : List.of(),
                                thumbnailUrl,
                                metadata != null ? metadata.viewCount() : 0
                        );
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                        return null;
                    }
                })
                .filter(item -> item != null)
                .toList();

        var sorted = switch (sort) {
            case "viewCount" -> list.stream()
                    .sorted(Comparator.comparingInt(ImageCollectionResponse::viewCount))
                    .toList();
            default -> list.stream()
                    .sorted(Comparator.comparing(ImageCollectionResponse::title))
                    .toList();
        };

        if ("desc".equals(order)) {
            return sorted.reversed();
        }
        return sorted;
    }

    private String getThumbnailUrl(String collectionId, File collectionDir) {
        var thumbnailName = ImageRepository.getThumbnailFileName(collectionDir);
        return "/storage/images/" + collectionId + "/" + thumbnailName;
    }

    /**
     * Returns image collection details.
     */
    public ImageDetailsResponse getCollectionDetails(String collectionId, boolean isPrivate) {
        SlugUtils.validateCollectionId(collectionId);
        var collectionDir = FileUtils.validatePath(imagesDir, collectionId);

        if (!collectionDir.exists() || !collectionDir.isDirectory()) {
            throw new NoSuchElementException("Collection not found: " + collectionId);
        }

        var metadata = getMetaData(collectionDir);

        var collectionIsPrivate = metadata != null && metadata.isPrivate();
        if (isPrivate != collectionIsPrivate) {
            throw new NoSuchElementException("Collection not found: " + collectionId);
        }

        var imageNames = ImageRepository.getImageFileNames(collectionDir);
        var imageUrls = imageNames.stream()
                .sorted()
                .map(name -> "/storage/images/" + collectionId + "/" + name)
                .toList();
        var thumbnailUrl = getThumbnailUrl(collectionId, collectionDir);
        FileUtils.incrementViewCount(collectionDir);

        return new ImageDetailsResponse(
                collectionId,
                collectionId,
                metadata != null ? metadata.title() : collectionId,
                metadata != null ? metadata.artist() : "",
                metadata != null && metadata.tags() != null ? metadata.tags() : List.of(),
                metadata != null ? metadata.description() : "",
                thumbnailUrl,
                imageUrls
        );
    }

    /**
     * Creates a new image collection.
     */
    public String createCollection(ImageUploadDto request, boolean isPrivate) {
        // validation for creating collection dir.
        logger.info("Images upload started: title={}", request.title());
        FileUtils.ensureExists(imagesDir);
        var collectionId = SlugUtils.toSlug(request.title());
        var collectionDir = new File(imagesDir, collectionId);

        if (collectionDir.exists()) {
            logger.warn("Collection already exists: {}", collectionId);
            throw new IllegalStateException("Collection already exists: " + collectionId);
        }

        var folderCreated = false;
        try {
            Files.createDirectories(collectionDir.toPath());

            folderCreated = true;

            var images = request.images();
            for (int i = 0; i < images.size(); i++) {
                var file = images.get(i);
                if (!file.isEmpty()) {
                    var safeName = Path.of(file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg").getFileName().toString();
                    var extension = FileUtils.getExtensionFromName(safeName, "jpg");
                    var fileName = String.format("%03d.%s", i + 1, extension);
                    var imageFile = new File(collectionDir, fileName);
                    file.transferTo(imageFile);
                }
            }

            if (request.thumbnail() != null && !request.thumbnail().isEmpty()) {
                var safeName = Path.of(request.thumbnail().getOriginalFilename() != null ? request.thumbnail().getOriginalFilename() : "thumbnail.jpg").getFileName().toString();
                var extension = FileUtils.getExtensionFromName(safeName, "jpg");
                var thumbnailFile = new File(collectionDir, "thumbnail." + extension);
                request.thumbnail().transferTo(thumbnailFile);
            } else {
                var files = collectionDir.listFiles();
                if (files != null) {
                    for (var f : files) {
                        if (FileUtils.isImageFile(f)) {
                            var ext = FileUtils.getExtension(f);
                            var dest = new File(collectionDir, "thumbnail." + ext);
                            Files.copy(f.toPath(), dest.toPath());
                            break;
                        }
                    }
                }
            }

            var metadata = new CollectionMetadata(
                    request.title(),
                    request.artist(),
                    request.tags(),
                    request.description() != null ? request.description() : ""
            ).withPrivate(isPrivate);
            FileUtils.saveMetaData(collectionDir, metadata);
            logger.info("Image upload successful: collectionId={}", collectionId);
            return collectionId;
        } catch (Exception e) {
            logger.error("Images upload failed: {} - {}", collectionId, e.getMessage(), e);
            if (folderCreated && collectionDir.exists()) {
                var deleted = FileUtils.deleteRecursively(collectionDir);
                if (deleted) {
                    logger.info("Rollback complete: deleted folder - {}", collectionId);
                } else {
                    logger.error("Rollback failed: unable to delete folder - {}", collectionId);
                }
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes an image collection.
     */
    public void deleteCollection(String collectionId) {
        SlugUtils.validateCollectionId(collectionId);
        var collectionDir = FileUtils.validatePath(imagesDir, collectionId);
        if (!collectionDir.exists() || !collectionDir.isDirectory()) {
            logger.warn("Collection not found for deletion: {}", collectionId);
            throw new NoSuchElementException("Collection not found: " + collectionId);
        }

        var deleted = FileUtils.deleteRecursively(collectionDir);
        if (!deleted) {
            logger.error("Failed to delete image collection: {}", collectionId);
            throw new IllegalStateException("Failed to delete collection: " + collectionId);
        }

        logger.info("Collection deleted: {}", collectionId);
    }

    /**
     * Updates collection metadata and optionally replaces the thumbnail.
     */
    public void updateCollectionMetaData(String collectionId, ImageUpdateDto request) {
        SlugUtils.validateCollectionId(collectionId);
        var collectionDir = FileUtils.validatePath(imagesDir, collectionId);
        if (!collectionDir.exists() || !collectionDir.isDirectory()) {
            logger.warn("Collection not found for update: {}", collectionId);
            throw new NoSuchElementException("Collection not found: " + collectionId);
        }

        var metadata = getMetaData(collectionDir);
        if (metadata == null) {
            throw new NoSuchElementException("Metadata not found for collection: " + collectionId);
        }

        var updated = new CollectionMetadata(
                request.title() != null ? request.title() : metadata.title(),
                request.artist() != null ? request.artist() : metadata.artist(),
                request.tags() != null ? request.tags() : metadata.tags(),
                request.description() != null ? request.description() : metadata.description(),
                metadata.viewCount(),
                metadata.isPrivate()
        );
        FileUtils.saveMetaData(collectionDir, updated);

        if (request.thumbnail() != null && !request.thumbnail().isEmpty()) {
            var oldThumbnailName = ImageRepository.getThumbnailFileName(collectionDir);
            if (oldThumbnailName != null) {
                var oldThumbnail = new File(collectionDir, oldThumbnailName);
                if (oldThumbnail.exists()) oldThumbnail.delete();
            }
            try {
                var safeName = request.thumbnail().getOriginalFilename();
                var extension = safeName != null ? FileUtils.getExtensionFromName(Path.of(safeName).getFileName().toString(), "jpg") : "jpg";
                var newThumbnail = new File(collectionDir, "thumbnail." + extension);
                request.thumbnail().transferTo(newThumbnail);
                logger.info("Thumbnail updated for collection: {}", collectionId);
            } catch (IOException e) {
                throw new RuntimeException("Failed to update thumbnail", e);
            }
        }
    }

    /**
     * Adds images to an existing collection.
     */
    public void addCollectionImages(String collectionId, List<MultipartFile> images) {
        SlugUtils.validateCollectionId(collectionId);
        var collectionDir = FileUtils.validatePath(imagesDir, collectionId);
        if (!collectionDir.exists() || !collectionDir.isDirectory()) {
            logger.warn("Collection not found for adding images: {}", collectionId);
            throw new NoSuchElementException("Collection not found: " + collectionId);
        }

        var existingCount = ImageRepository.getImageFileNames(collectionDir).size();
        for (int i = 0; i < images.size(); i++) {
            var file = images.get(i);
            try {
                var safeName = file.getOriginalFilename() != null ? Path.of(file.getOriginalFilename()).getFileName().toString() : null;
                var extension = safeName != null ? FileUtils.getExtensionFromName(safeName, "jpg") : "jpg";
                var newName = String.format("%03d.%s", existingCount + 1 + i, extension);
                file.transferTo(new File(collectionDir, newName));
            } catch (IOException e) {
                throw new RuntimeException("Failed to save image", e);
            }
        }
        logger.info("Added {} images to collection: {}", images.size(), collectionId);
    }

    public void repairImageCollection(String collectionId) {
        SlugUtils.validateCollectionId(collectionId);
        var collectionDir = FileUtils.validatePath(imagesDir, collectionId);
        if (!collectionDir.exists() || !collectionDir.isDirectory()) {
            throw new NoSuchElementException("Collection not found: " + collectionId);
        }

        if (getMetaData(collectionDir) == null) {
            saveMetaData(collectionDir, new CollectionMetadata(collectionDir.getName()));
        }

        if (ImageRepository.getThumbnailFileName(collectionDir) == null) {
            var files = collectionDir.listFiles();
            if (files == null) return;
            for (File file : files) {
                if (!FileUtils.isImageFile(file)) continue;
                var ext = FileUtils.getExtension(file);
                var thumbNailFile = new File(collectionDir, "thumbnail." + ext);
                try {
                    Files.copy(file.toPath(), thumbNailFile.toPath());
                } catch (IOException e) {
                    throw new UncheckedIOException("UnCheckedIOException", e);
                }
                break;
            }
        }
    }

}