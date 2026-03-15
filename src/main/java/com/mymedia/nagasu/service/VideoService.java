package com.mymedia.nagasu.service;

import com.mymedia.nagasu.dto.VideoCollectionResponse;
import com.mymedia.nagasu.dto.VideoDetailsResponse;
import com.mymedia.nagasu.dto.VideoUploadRequestDto;
import com.mymedia.nagasu.dto.metadata.CollectionMetadata;
import com.mymedia.nagasu.repository.ImageRepository;
import com.mymedia.nagasu.repository.VideoRepository;
import com.mymedia.nagasu.utils.FfmpegUtils;
import com.mymedia.nagasu.utils.FileUtils;
import com.mymedia.nagasu.utils.SlugUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import static com.mymedia.nagasu.utils.FileUtils.*;

@Service
public class VideoService {

    private final File videoDir;
    private static final Logger logger = LoggerFactory.getLogger(VideoService.class);

    public VideoService(@Value("${storage.path}") String storagePath) {
        this.videoDir = new File(storagePath, "videos");
    }

    /**
     * Returns all video collections.
     */
    public List<VideoCollectionResponse> getVideoCollection(String sort, String order) {
        FileUtils.ensureExists(videoDir);
        var list = VideoRepository.getVideoCollection(videoDir).stream()
                .map(folderName -> {
                    try {
                        var folder = new File(videoDir, folderName);
                        var thumbnailUrl = getThumbnailUrl(folder);
                        var metadata = getMetaData(folder);

                        return new VideoCollectionResponse(
                                folderName,
                                metadata != null ? metadata.title() : folderName,
                                metadata != null ? metadata.artist() : "",
                                metadata != null && metadata.tags() != null ? metadata.tags() : List.of(),
                                thumbnailUrl,
                                metadata != null ? metadata.viewCount() : 0
                        );
                    } catch (Exception e) {
                        logger.warn("Failed to load collection: {} - {}", folderName, e.getMessage());
                        return null;
                    }
                })
                .filter(item -> item != null)
                .toList();

        var sorted = switch (sort) {
            case "viewCount" -> list.stream()
                    .sorted(Comparator.comparingInt(VideoCollectionResponse::viewCount))
                    .toList();
            default -> list.stream()
                    .sorted(Comparator.comparing(VideoCollectionResponse::title))
                    .toList();
        };
        if ("desc".equals(order)) {
            return sorted.reversed();
        }
        return sorted;
    }

    /**
     * Returns video collection details.
     */
    public VideoDetailsResponse getVideoCollectionDetails(String collectionId) {
        try {
            SlugUtils.validateCollectionId(collectionId);
            var videoColDir = FileUtils.validatePath(videoDir, collectionId);
            FileUtils.ensureExists(videoDir);

            if (!videoColDir.exists() || !videoColDir.isDirectory()) {
                throw new NoSuchElementException("Video collection not found: " + collectionId);
            }

            var metadata = getMetaData(videoColDir);
            if (metadata != null) {
                saveMetaData(videoColDir, metadata.viewCountIncrement());
            }
            var thumbnailUrl = getThumbnailUrl(videoColDir);
            if (thumbnailUrl == null) {
                throw new IllegalStateException("Thumbnail not found: " + collectionId);
            }
            var videoUrl = getVideoUrl(videoColDir);
            if (videoUrl == null) {
                throw new IllegalStateException("Video file not found: " + collectionId);
            }
            var videoSubtitleUrl = getVideoSubtitle(videoColDir);

            return new VideoDetailsResponse(
                    collectionId,
                    videoColDir.getName(),
                    metadata != null ? metadata.title() : videoColDir.getName(),
                    metadata != null ? metadata.artist() : "Unknown",
                    metadata != null && metadata.tags() != null ? metadata.tags() : List.of(),
                    metadata != null ? metadata.description() : "",
                    thumbnailUrl,
                    videoUrl,
                    videoSubtitleUrl,
                    metadata != null ? metadata.viewCount() : 0
            );
        } catch (NoSuchElementException e) {
            logger.warn("Collection not found: {}", collectionId);
            throw e;
        } catch (IllegalStateException e) {
            logger.error("Collection state error: {} - {}", collectionId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to get collection: {} - {}", collectionId, e.getMessage(), e);
            throw new IllegalStateException("Failed to get collection: " + collectionId, e);
        }
    }

    /**
     * Uploads a video collection.
     */
    public String uploadVideoCollection(VideoUploadRequestDto videoUploadRequestDto) {
        logger.info("Video upload started: title={}", videoUploadRequestDto.title());
        FileUtils.ensureExists(videoDir);

        var rawFilename = videoUploadRequestDto.video().getOriginalFilename();
        if (rawFilename == null || rawFilename.isBlank()) {
            throw new IllegalArgumentException("Filename is missing or blank");
        }
        var originalFileName = Path.of(rawFilename).getFileName().toString();
        var baseName = getNameBeforeLastDot(originalFileName);
        var collectionId = SlugUtils.toSlug(baseName);
        if (collectionId.isBlank()) {
            throw new IllegalArgumentException("Invalid filename: cannot generate collection ID");
        }
        SlugUtils.validateCollectionId(collectionId);

        var collectionDir = FileUtils.validatePath(videoDir, collectionId);

        if (collectionDir.exists()) {
            logger.warn("collection already exist: {}", collectionId);
            throw new IllegalStateException("collection already exist: " + collectionId);
        }

        var folderCreated = false;
        try {
            collectionDir.mkdirs();
            folderCreated = true;

            // Save video (keep original filename)
            var videoFile = new File(collectionDir, originalFileName);
            videoUploadRequestDto.video().transferTo(videoFile);

            // Thumbnail processing
            var thumbnailInput = videoUploadRequestDto.thumbnail();
            if (thumbnailInput != null && !thumbnailInput.isEmpty()) {
                var thumbnailFileName = Path.of(
                        thumbnailInput.getOriginalFilename() != null ? thumbnailInput.getOriginalFilename() : "thumbnail.jpg"
                ).getFileName().toString();
                var thumbnailFile = new File(collectionDir, thumbnailFileName);
                thumbnailInput.transferTo(thumbnailFile);
            } else {
                var thumbnailFile = FfmpegUtils.extractThumbnailToFolder(videoFile);
                if (thumbnailFile == null) {
                    logger.error("thumbnailFile create failed");
                    throw new IllegalStateException("thumbnailFile create failed");
                }
            }

            // Save subtitle and convert to VTT
            var subtitleInput = videoUploadRequestDto.subtitle();
            if (subtitleInput != null && !subtitleInput.isEmpty()) {
                var safeSubtitleName = Path.of(
                        subtitleInput.getOriginalFilename() != null ? subtitleInput.getOriginalFilename() : "subtitle.srt"
                ).getFileName().toString();
                var subtitleExtension = getExtensionFromName(safeSubtitleName, "srt");
                var subtitleFileName = baseName + "." + subtitleExtension;
                var subtitleFile = new File(collectionDir, subtitleFileName);
                subtitleInput.transferTo(subtitleFile);

                if (!subtitleExtension.equalsIgnoreCase("vtt")) {
                    var vttFile = new File(collectionDir, baseName + ".vtt");
                    FfmpegUtils.convertToVtt(subtitleFile, vttFile);
                }
            }

            // Save metadata
            var metadata = new CollectionMetadata(
                    videoUploadRequestDto.title(),
                    videoUploadRequestDto.artist(),
                    videoUploadRequestDto.tags(),
                    videoUploadRequestDto.description() != null
                            ? videoUploadRequestDto.description() : ""
            );
            FileUtils.saveMetaData(collectionDir, metadata);
            logger.info("Video upload successful: collectionId={}", collectionId);
            return collectionId;
        } catch (Exception e) {
            logger.error("Video upload failed: {} - {}", collectionId, e.getMessage(), e);
            if (folderCreated && collectionDir.exists()) {
                var deleted = deleteRecursively(collectionDir);
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
     * Deletes a video collection.
     */
    public void deleteVideoCollection(String collectionId) {
        SlugUtils.validateCollectionId(collectionId);
        var collectionDir = FileUtils.validatePath(videoDir, collectionId);
        logger.info("Deleting video collection: {}", collectionId);
        if (!collectionDir.exists() || !collectionDir.isDirectory()) {
            logger.warn("Collection not found for deletion: {}", collectionId);
            throw new NoSuchElementException("Collection not found: " + collectionId);
        }

        var deleted = deleteRecursively(collectionDir);
        if (!deleted) {
            logger.error("Failed to delete video collection: {}", collectionId);
            throw new IllegalStateException("Failed to delete collection: " + collectionId);
        }

        logger.info("Video collection deleted: {}", collectionId);
    }

    /**
     * Repairs a manually added video collection.
     */
    public void repairVideoCollection(String collectionId) {
        SlugUtils.validateCollectionId(collectionId);
        var collectionDir = FileUtils.validatePath(videoDir, collectionId);
        if (!collectionDir.exists() || !collectionDir.isDirectory()) {
            throw new IllegalArgumentException("No Such Collection " + collectionId);
        }

        var files = collectionDir.listFiles();
        var fileList = files != null ? Arrays.asList(files) : List.<File>of();

        var videoFile = fileList.stream()
                .filter(FileUtils::isVideoFile)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No Such Video " + collectionId));

        if (getMetaData(collectionDir) == null) {
            FileUtils.saveMetaData(collectionDir, new CollectionMetadata(FileUtils.getNameWithoutExtension(videoFile)));
        }

        if (fileList.stream().noneMatch(FileUtils::isImageFile)) {
            FfmpegUtils.extractThumbnailToFolder(videoFile);
        }

        var srtFile = fileList.stream()
                .filter(f -> FileUtils.getExtension(f).equalsIgnoreCase("srt"))
                .findFirst()
                .orElse(null);
        var hasVtt = fileList.stream()
                .anyMatch(f -> FileUtils.getExtension(f).equalsIgnoreCase("vtt"));

        if (srtFile != null && !hasVtt) {
            var vttFile = new File(collectionDir, FileUtils.getNameWithoutExtension(srtFile) + ".vtt");
            FfmpegUtils.convertToVtt(srtFile, vttFile);
        }
    }

    private String getThumbnailUrl(File collectionDir) {
        var thumbnailName = ImageRepository.getThumbnailFileName(collectionDir);
        if (thumbnailName == null) return null;
        return "/storage/videos/" + collectionDir.getName() + "/" + thumbnailName;
    }

    private String getVideoUrl(File collectionDir) {
        var videoFileName = VideoRepository.getVideoFileName(collectionDir);
        if (videoFileName == null) return null;
        return "/storage/videos/" + collectionDir.getName() + "/" + videoFileName;
    }

    private String getVideoSubtitle(File collectionDir) {
        var subtitleName = VideoRepository.getSubtitleFileName(collectionDir);
        if (subtitleName == null) return null;
        return "/storage/videos/" + collectionDir.getName() + "/" + subtitleName;
    }

    private static String getNameBeforeLastDot(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(0, dot) : filename;
    }

    private static String getExtensionFromName(String filename, String defaultExt) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1) : defaultExt;
    }

    private static boolean deleteRecursively(File file) {
        if (file.isDirectory()) {
            var children = file.listFiles();
            if (children != null) {
                for (var child : children) {
                    deleteRecursively(child);
                }
            }
        }
        return file.delete();
    }
}