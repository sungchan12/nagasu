package com.mymedia.nagasu.service

import com.mymedia.nagasu.dto.VideoCollectionResponse
import com.mymedia.nagasu.dto.VideoDetailsResponse
import com.mymedia.nagasu.dto.VideoUploadRequestDto
import com.mymedia.nagasu.dto.metadata.CollectionMetadata
import com.mymedia.nagasu.repository.getSubtitleFileName
import com.mymedia.nagasu.repository.getThumbnailFileName
import com.mymedia.nagasu.repository.getVideoCollection
import com.mymedia.nagasu.repository.getVideoFileName
import com.mymedia.nagasu.utils.ensureExists
import com.mymedia.nagasu.utils.convertToVtt
import com.mymedia.nagasu.utils.extractThumbnailToFolder
import com.mymedia.nagasu.utils.getMetaData
import com.mymedia.nagasu.utils.incrementViewCount
import com.mymedia.nagasu.utils.isImageFile
import com.mymedia.nagasu.utils.isVideoFile
import com.mymedia.nagasu.utils.saveMetaData
import com.mymedia.nagasu.utils.toSlug
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import org.slf4j.LoggerFactory
import java.nio.file.Path

/**
 * Video collection management service
 *
 * Video files are stored in the following structure:
 * {storagePath}/videos/{collectionId}/
 *   ├── {originalFileName}.mp4   (original video)
 *   ├── {baseName}.srt           (original subtitle, optional)
 *   ├── {baseName}.vtt           (converted subtitle, optional)
 *   ├── thumbnail.jpg            (thumbnail)
 *   └── metadata.json            (metadata)
 *
 * collectionId is a slugified version of the video file's baseName.
 */
@Service
class VideoService(
    @Value("\${storage.path}") private val storagePath: String) {
    private val videoDir = File(storagePath, "videos")
    private val logger = LoggerFactory.getLogger(VideoService::class.java)

    /**
     * Returns all video collections.
     * Skips collections that fail to load.
     */
    fun getVideoCollection(sort: String, order: String): List<VideoCollectionResponse> {
        videoDir.ensureExists()
        val list = videoDir.getVideoCollection()
            .mapNotNull { folderName ->
                try {
                    val folder = File(videoDir, folderName)
                    val thumbnailUrl = getThumbnailUrl(folder)
                    val metadata = folder.getMetaData() //nullable

                    VideoCollectionResponse(
                        id = folderName,
                        title = metadata?.title ?: folderName,
                        artist = metadata?.artist ?: "",
                        tags = metadata?.tags ?: emptyList(),
                        thumbnailUrl = thumbnailUrl,
                        viewCount = metadata?.viewCount ?: 0
                    )
                } catch (e: Exception) {
                    logger.warn("Failed to load collection: $folderName - ${e.message}")
                    null
                }
            }
        val sorted = when (sort) {
            "viewCount" -> list.sortedBy { it.viewCount }
            else -> list.sortedBy { it.title }
        }
        return if (order == "desc") {
            sorted.reversed()
        } else sorted
    }

    /**
     * Returns video collection details.
     * @param collectionId slugified collection folder name
     * @throws NoSuchElementException if collection does not exist
     * @throws IllegalStateException if thumbnail or video file is missing
     */
    fun getVideoCollectionDetails(collectionId: String): VideoDetailsResponse {
        return try {
            val videoColDir = File(videoDir, collectionId)
            videoDir.ensureExists()

            if (!videoColDir.exists() || !videoColDir.isDirectory) {
                throw NoSuchElementException("Video collection not found: $collectionId")
            }
            videoColDir.incrementViewCount()
            val metadata = videoColDir.getMetaData()
            val thumbnailUrl = getThumbnailUrl(videoColDir)
                ?: throw IllegalStateException("Thumbnail not found: $collectionId")
            val videoUrl = getVideoUrl(videoColDir)
                ?: throw IllegalStateException("Video file not found: $collectionId")
            val videoSubtitleUrl = getVideoSubtitle(videoColDir)

            VideoDetailsResponse(
                id = collectionId,
                name = videoColDir.name,
                title = metadata?.title ?: videoColDir.name,
                artist = metadata?.artist ?: "Unknown",
                tags = metadata?.tags ?: emptyList(),
                description = metadata?.description ?: "",
                thumbnailUrl = thumbnailUrl,
                videoUrl = videoUrl,
                videoSubtitleUrl = videoSubtitleUrl,
                viewCount = videoColDir.getMetaData()?.viewCount ?: 0
            )
        } catch (e: NoSuchElementException) {
            logger.warn("Collection not found: $collectionId")
            throw e
        } catch (e: IllegalStateException) {
            logger.error("Collection state error: $collectionId - ${e.message}")
            throw e
        } catch (e: Exception) {
            logger.error("Failed to get collection: $collectionId - ${e.message}", e)
            throw IllegalStateException("Failed to get collection: $collectionId", e)
        }
    }

    /**
     * Uploads a video collection.
     * Slugifies the file's baseName to use as collectionId (folder name).
     * Rolls back created folder on failure.
     * @return created collectionId
     * @throws IllegalArgumentException if filename is missing
     * @throws IllegalStateException if collectionId already exists
     */
    fun uploadVideoCollection(videoUploadRequestDto: VideoUploadRequestDto): String {
        logger.info("Video upload started: title=${videoUploadRequestDto.title}")
        videoDir.ensureExists()

        // Extract baseName from filename and slugify as collectionId
        val originalFileName = Path.of(videoUploadRequestDto.video.originalFilename
            ?: throw IllegalArgumentException("Filename is missing")).fileName.toString()
        val baseName = originalFileName.substringBeforeLast('.')
        val collectionId = toSlug(baseName)

        val collectionDir = File(videoDir, collectionId)

        // Reject upload if collectionId already exists
        if (collectionDir.exists()) {
            logger.warn("collection already exist: $collectionId")
            throw IllegalStateException("collection already exist: $collectionId")
        }

        var folderCreated = false
        try {
            collectionDir.mkdirs()
            folderCreated = true

            // Save video (keep original filename)
            val videoFile = File(collectionDir, originalFileName)
            videoUploadRequestDto.video.transferTo(videoFile)

            // Thumbnail processing
            val thumbnailInput = videoUploadRequestDto.thumbnail
            if (thumbnailInput != null && !thumbnailInput.isEmpty) {
                // Save user-provided thumbnail
                val thumbnailFileName = Path.of(thumbnailInput.originalFilename ?: "thumbnail.jpg").fileName.toString()
                val thumbnailFile = File(collectionDir, thumbnailFileName)
                thumbnailInput.transferTo(thumbnailFile)
            } else {
                // Auto-generate thumbnail from video via FFmpeg
                val thumbnailFile = extractThumbnailToFolder(videoFile)
                if (thumbnailFile == null) {
                    logger.error("thumbnailFile create failed")
                    throw IllegalStateException("thumbnailFile create failed")
                }
            }

            // Save subtitle and convert to VTT
            val subtitleInput = videoUploadRequestDto.subtitle
            if (subtitleInput != null && !subtitleInput.isEmpty) {
                val safeSubtitleName = Path.of(subtitleInput.originalFilename ?: "subtitle.srt").fileName.toString()
                val subtitleExtension = safeSubtitleName.substringAfterLast('.', "srt")
                val subtitleFileName = "$baseName.$subtitleExtension"
                val subtitleFile = File(collectionDir, subtitleFileName)
                subtitleInput.transferTo(subtitleFile)

                // Convert to VTT if not already in VTT format
                if (!subtitleExtension.equals("vtt", ignoreCase = true)) {
                    val vttFile = File(collectionDir, "$baseName.vtt")
                    convertToVtt(subtitleFile, vttFile)
                }
            }

            // Save metadata
            val metadata = CollectionMetadata(
                title = videoUploadRequestDto.title,
                artist = videoUploadRequestDto.artist,
                tags = videoUploadRequestDto.tags,
                description = videoUploadRequestDto.description ?: ""
            )
            collectionDir.saveMetaData(metadata)
            logger.info("Video upload successful: collectionId=$collectionId")
            return collectionId
        } catch (e: Exception) {
            logger.error("Video upload failed: $collectionId - ${e.message}", e)
            // Rollback: delete folder created by this request on failure
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
     * Deletes an entire video collection folder.
     * @throws NoSuchElementException if collection does not exist
     * @throws IllegalStateException if deletion fails
     */
    fun deleteVideoCollection(collectionId: String) {
        logger.info("Deleting video collection: $collectionId")
        val collectionDir = File(videoDir, collectionId)

        if (!collectionDir.exists() || !collectionDir.isDirectory) {
            logger.warn("Collection not found for deletion: $collectionId")
            throw NoSuchElementException("Collection not found: $collectionId")
        }

        val deleted = collectionDir.deleteRecursively()
        if (!deleted) {
            logger.error("Failed to delete video collection: $collectionId")
            throw IllegalStateException("Failed to delete collection: $collectionId")
        }

        logger.info("Video collection deleted: $collectionId")
    }
    
    /**
     * Repairs a manually added video collection.
     *
     * Checks the following in order and auto-generates missing items:
     * 1. Collection folder exists (404 if not)
     * 2. Video file exists (400 if not, cannot repair)
     * 3. Creates metadata.json with defaults if missing
     * 4. Extracts thumbnail.jpg via FFmpeg at 1s if missing
     * 5. Converts .srt to .vtt via FFmpeg if .vtt is missing
     *
     * @param collectionId slugified collection folder name
     * @throws NoSuchElementException if collection folder does not exist
     * @throws IllegalStateException if video file is missing and repair is impossible
     */
    fun repairVideoCollection(collectionId: String) {
        val collectionDir = File(videoDir, collectionId)
        if (!collectionDir.exists() || !collectionDir.isDirectory) {
            throw IllegalArgumentException("No Such Collection $collectionId")
        }
        val files = collectionDir.listFiles()?.toList() ?: emptyList()

        val videoFile = files.firstOrNull { it.isVideoFile() } ?: throw NoSuchElementException("No Such Video $collectionId")
        if (collectionDir.getMetaData() == null) {
            collectionDir.saveMetaData(
                CollectionMetadata(
                    title = videoFile.nameWithoutExtension
                )
            )
        }
        if (files.none { it.isImageFile() }) { extractThumbnailToFolder(videoFile) }

        val srtFile = files.firstOrNull { it.extension.equals("srt", ignoreCase = true) }
        val hasVtt = files.any { it.extension.equals("vtt", ignoreCase = true) }

        if (srtFile != null && !hasVtt) {
            val vttFile = File(collectionDir, "${srtFile.nameWithoutExtension}.vtt")
            convertToVtt(srtFile, vttFile)
        }
    }
    // Generate thumbnail URL (/storage/videos/{collectionId}/{thumbnailFileName})
    private fun getThumbnailUrl(collectionDir: File): String? {
        val thumbnailName = collectionDir.getThumbnailFileName() ?: return null
        return "/storage/videos/${collectionDir.name}/$thumbnailName"
    }

    // Generate video URL (/storage/videos/{collectionId}/{videoFileName})
    private fun getVideoUrl(collectionDir: File): String? {
        val videoFileName = collectionDir.getVideoFileName() ?: return null
        return "/storage/videos/${collectionDir.name}/$videoFileName"
    }

    // Generate subtitle URL (/storage/videos/{collectionId}/{subtitleFileName})
    private fun getVideoSubtitle(collectionDir: File): String? {
        val videoSubtitle = collectionDir.getSubtitleFileName() ?: return null
        return "/storage/videos/${collectionDir.name}/$videoSubtitle"
    }
}
