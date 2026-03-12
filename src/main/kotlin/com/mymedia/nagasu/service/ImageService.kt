package com.mymedia.nagasu.service

import com.mymedia.nagasu.dto.ImageCollectionResponse
import com.mymedia.nagasu.dto.ImageDetailsResponse
import com.mymedia.nagasu.dto.ImageUpdateDto
import com.mymedia.nagasu.dto.ImageUploadDto
import com.mymedia.nagasu.dto.metadata.CollectionMetadata
import com.mymedia.nagasu.utils.toSlug
import com.mymedia.nagasu.utils.ensureExists
import com.mymedia.nagasu.utils.getMetaData
import com.mymedia.nagasu.repository.getCollectionDirs
import com.mymedia.nagasu.repository.getThumbnailFileName
import com.mymedia.nagasu.repository.getImageFileNames
import com.mymedia.nagasu.utils.incrementViewCount
import com.mymedia.nagasu.utils.isImageFile
import com.mymedia.nagasu.utils.saveMetaData
import com.mymedia.nagasu.utils.validateCollectionId
import com.mymedia.nagasu.utils.validatePath
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Path
import java.util.Collections.list

/**
 * Image collection management service
 */
@Service
class ImageService(
    @Value("\${storage.path}")
    private val storagePath: String) {
    private val imagesDir = File(storagePath, "images")
    private val logger = LoggerFactory.getLogger(ImageService::class.java)
    /**
     * Returns all image collections.
     * Skips collections that fail to load.
     */
    fun getCollections(sort: String, order: String): List<ImageCollectionResponse> {
        imagesDir.ensureExists()
        val list = imagesDir.getCollectionDirs().mapNotNull {
            folderName ->
            try {
                val folder = File(imagesDir, folderName)
                val thumbnailFileUrl = getThumbnailUrl(folderName, folder)
                val metadata = folder.getMetaData()

                ImageCollectionResponse(
                    id = folderName,
                    name = folderName,
                    title = metadata?.title ?: folderName,
                    artist = metadata?.artist ?: "Unknown",
                    tags = metadata?.tags ?: emptyList(),
                    thumbnailUrl = thumbnailFileUrl,
                    viewCount = metadata?.viewCount ?: 0
                )
            } catch (e: Exception) {
                logger.error(e.message)
                null
            }
        }
        val sorted = when (sort) {
            "viewCount" -> list.sortedBy { it.viewCount }
            else -> list.sortedBy { it.title }
        }
        return if (order == "desc") sorted.reversed() else sorted
    }

    /** Generates thumbnail URL for the given collection. */
    private fun getThumbnailUrl(collectionId: String, collectionDir: File): String {
        val thumbnailName = collectionDir.getThumbnailFileName()
        return "/storage/images/$collectionId/$thumbnailName"
    }

    /**
     * Returns image collection details.
     * @throws NoSuchElementException if collection does not exist
     */
    fun getCollectionDetails(collectionId: String): ImageDetailsResponse {
        validateCollectionId(collectionId)
        val collectionDir = validatePath(imagesDir, collectionId)

        if (!collectionDir.exists() || !collectionDir.isDirectory) throw NoSuchElementException("Collection not found: $collectionId")

        val metadata = collectionDir.getMetaData()
        val imageNames = collectionDir.getImageFileNames()
        val imageUrls = imageNames.map { "/storage/images/$collectionId/$it" }
        val thumbnailUrl = getThumbnailUrl(collectionId, collectionDir)
        collectionDir.incrementViewCount()

        return ImageDetailsResponse(
            id = collectionId,
            name = collectionId,
            title = metadata?.title ?: collectionId,
            artist = metadata?.artist ?: "",
            tags = metadata?.tags ?: emptyList(),
            description = metadata?.description ?: "",
            thumbnailUrl = thumbnailUrl,
            images = imageUrls
        )
    }

    /**
     * Creates a new image collection.
     * Rolls back created folder on failure.
     * @return created collectionId
     * @throws IllegalStateException if collectionId already exists
     */
    fun createCollection(request: ImageUploadDto): String {
        logger.info("Images upload started: title=${request.title}")
        imagesDir.ensureExists()
        val collectionId = toSlug(request.title)
        val collectionDir = File(imagesDir, collectionId)

        if (collectionDir.exists()) {
            logger.warn("Collection already exists: $collectionId")
            throw IllegalStateException("Collection already exists: $collectionId")
        }

        var folderCreated = false
        try {
            collectionDir.mkdirs()
            folderCreated = true
            request.images.forEachIndexed { index, file ->
                if (!file.isEmpty) {
                    val safeName = Path.of(file.originalFilename ?: "image.jpg").fileName.toString()
                    val extension = safeName.substringAfterLast('.', "jpg")
                    val fileName = String.format("%03d.%s", index + 1, extension)
                    val imageFile = File(collectionDir, fileName)
                    file.transferTo(imageFile)
                }
            }
            if (request.thumbnail != null && !request.thumbnail.isEmpty) {
                val safeName = Path.of(request.thumbnail.originalFilename ?: "thumbnail.jpg").fileName.toString()
                val extension = safeName.substringAfterLast('.', "jpg")
                val thumbnailFile = File(collectionDir, "thumbnail.${extension}")
                request.thumbnail.transferTo(thumbnailFile)
            } else {
                val firstImage = collectionDir.listFiles()?.firstOrNull { it.isImageFile() }
                firstImage?.copyTo(File(collectionDir, "thumbnail.${firstImage.extension}"))
            }
            val metadata = CollectionMetadata(
                title = request.title,
                artist = request.artist,
                tags = request.tags,
                description = request.description ?: ""
            )
            collectionDir.saveMetaData(metadata)
            logger.info("Image upload successful: collectionId=$collectionId")
            return collectionId
        } catch (e: Exception) {
            logger.error("Images upload failed: $collectionId - ${e.message}", e)
            if (folderCreated && collectionDir.exists()) {
                val deleted = collectionDir.deleteRecursively()
                if (deleted) {
                    logger.info("Rollback complete: deleted folder - $collectionId")
                } else {
                    logger.error("Rollback failed: unable to delete folder - $collectionId")
                }
            }
            throw e
        }
    }

    /**
     * Deletes an image collection.
     * @throws NoSuchElementException if collection does not exist
     * @throws IllegalStateException if deletion fails
     */
    fun deleteCollection(collectionId: String) {
        validateCollectionId(collectionId)
        val collectionDir = validatePath(imagesDir, collectionId)
        if (!collectionDir.exists() || !collectionDir.isDirectory) {
            logger.warn("Collection not found for deletion: $collectionId")
            throw NoSuchElementException("Collection not found: $collectionId")
        }

        val deleted = collectionDir.deleteRecursively()
        if (!deleted) {
            logger.error("Failed to delete image collection: $collectionId")
            throw IllegalStateException("Failed to delete collection: $collectionId")
        }

        logger.info("Collection deleted: $collectionId")
    }

    /**
     * Updates collection metadata and optionally replaces the thumbnail.
     * @throws NoSuchElementException if collection or metadata does not exist
     */
    fun updateCollectionMetaData(collectionId: String, request: ImageUpdateDto) {
        validateCollectionId(collectionId)
        val collectionDir = validatePath(imagesDir, collectionId)
        if (!collectionDir.exists() || !collectionDir.isDirectory) {
            logger.warn("Collection not found for deletion(update): $collectionId")
            throw NoSuchElementException("Collection not found: $collectionId")
        }
        val metadata = collectionDir.getMetaData()
            ?: throw NoSuchElementException("Metadata not found for collection: $collectionId")
        val updated = metadata.copy(
            title = request.title ?: metadata.title,
            artist = request.artist ?: metadata.artist,
            tags = request.tags ?: metadata.tags,
            description = request.description ?: metadata.description
        )
        collectionDir.saveMetaData(updated)

        if (request.thumbnail != null && !request.thumbnail.isEmpty) {
            val oldThumbnailName = collectionDir.getThumbnailFileName()
            if (oldThumbnailName != null) {
                val oldThumbnail = File(collectionDir, oldThumbnailName)
                if (oldThumbnail.exists()) oldThumbnail.delete()
            }
            val safeName = request.thumbnail.originalFilename?.let { Path.of(it).fileName.toString() }
            val extension = safeName?.substringAfterLast('.', "jpg") ?: "jpg"
            val newThumbnail = File(collectionDir, "thumbnail.$extension")
            request.thumbnail.transferTo(newThumbnail)
            logger.info("Thumbnail updated for collection: $collectionId")
        }
    }

    /**
     * Adds images to an existing collection.
     * New images are numbered sequentially after existing ones.
     * @throws NoSuchElementException if collection does not exist
     */
    fun addCollectionImages(collectionId: String, images: List<MultipartFile>) {
        validateCollectionId(collectionId)
        val collectionDir = validatePath(imagesDir, collectionId)
        if (!collectionDir.exists() || !collectionDir.isDirectory) {
            logger.warn("Collection not found for adding images: $collectionId")
            throw NoSuchElementException("Collection not found: $collectionId")
        }
        val existingCount = collectionDir.getImageFileNames().size
        images.forEachIndexed { index, file ->
            val safeName = file.originalFilename?.let { Path.of(it).fileName.toString() }
            val extension = safeName?.substringAfterLast('.', "jpg") ?: "jpg"
            val newName = "%03d.%s".format(existingCount + 1 + index, extension)
            file.transferTo(File(collectionDir, newName))
        }
        logger.info("Added ${images.size} images to collection: $collectionId")
    }
}