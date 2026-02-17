package com.mymedia.nagasu.service

import com.mymedia.nagasu.dto.ImageCollectionResponse
import com.mymedia.nagasu.dto.ImageDetailsResponse
import com.mymedia.nagasu.dto.ImageUploadDto
import com.mymedia.nagasu.dto.metadata.CollectionMetadata
import com.mymedia.nagasu.utils.toSlug
import com.mymedia.nagasu.utils.ensureExists
import com.mymedia.nagasu.utils.getMetaData
import com.mymedia.nagasu.repository.getCollectionDirs
import com.mymedia.nagasu.repository.getThumbnailFileName
import com.mymedia.nagasu.repository.getImageFileNames
import com.mymedia.nagasu.utils.isImageFile
import com.mymedia.nagasu.utils.saveMetaData
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Path

/**
 * Image collection management service
 */
@Service
class ImageService(
    @Value("\${storage.path}") private val storagePath: String) {
    private val imagesDir = File(storagePath, "images")
    private val logger = LoggerFactory.getLogger(ImageService::class.java)
    /**
     * Returns all image collections.
     * Skips collections that fail to load.
     */
    fun getCollections(): List<ImageCollectionResponse> {
        imagesDir.ensureExists()

        return imagesDir.getCollectionDirs()
            .mapNotNull { folderName ->
                try {
                    val folder = File(imagesDir, folderName)
                    val thumbnailUrl = getThumbnailUrl(folderName, folder)
                    val metadata = folder.getMetaData()

                    ImageCollectionResponse(
                        id = folderName,
                        name = folderName,
                        title = metadata?.title ?: folderName,
                        artist = metadata?.artist ?: "",
                        tags = metadata?.tags ?: emptyList(),
                        thumbnailUrl = thumbnailUrl
                    )
                } catch (e: Exception) {
                    logger.warn("Failed to load collection: $folderName - ${e.message}")
                    null
                }
            }
    }

    private fun getThumbnailUrl(collectionId: String, collectionDir: File): String {
        val thumbnailName = collectionDir.getThumbnailFileName()
        return "/storage/images/$collectionId/$thumbnailName"
    }

    /**
     * Returns image collection details.
     * @throws NoSuchElementException if collection does not exist
     */
    fun getCollectionDetails(collectionId: String): ImageDetailsResponse {
        val collectionDir = File(imagesDir, collectionId)
        if (!collectionDir.exists() || !collectionDir.isDirectory) throw NoSuchElementException("Collection not found: $collectionId")

        val metadata = collectionDir.getMetaData()
        val imageNames = collectionDir.getImageFileNames()
        val imageUrls = imageNames.map { "/storage/images/$collectionId/$it" }
        val thumbnailUrl = getThumbnailUrl(collectionId, collectionDir)

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
        val collectionDir = File(imagesDir, collectionId)
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
}