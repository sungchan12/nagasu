package com.mymedia.streamer.service

import com.mymedia.streamer.dto.ImageCollectionResponse
import com.mymedia.streamer.dto.ImageDetailsResponse
import com.mymedia.streamer.dto.ImageUploadDto
import com.mymedia.streamer.dto.ImageUploadResponse
import com.mymedia.streamer.dto.metadata.ImageMetadata
import com.mymedia.streamer.utils.toSlug
import com.mymedia.streamer.utils.ensureExists
import com.mymedia.streamer.repository.getCollectionDirs
import com.mymedia.streamer.repository.getThumbnailFileName
import com.mymedia.streamer.repository.getImageFileNames
import com.mymedia.streamer.repository.getMetaData
import com.mymedia.streamer.repository.saveMetaData
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File

/**
 * 이미지 컬렉션 관리 서비스
 */
@Service
class ImageService(
    @Value("\${storage.path}") private val storagePath: String
) {
    private val imagesDir: File get() = File(storagePath, "images")

    /**
     * 이미지 컬렉션(폴더) 목록을 조회한다.
     */
    fun getCollections(): List<ImageCollectionResponse> {
        imagesDir.ensureExists()

        return imagesDir.getCollectionDirs()
            .mapNotNull { folder ->
                val thumbnailUrl = getThumbnailUrl(folder.name, folder) ?: return@mapNotNull null
                val metadata = folder.getMetaData()

                ImageCollectionResponse(
                    id = folder.name,
                    name = folder.name,
                    title = metadata?.title ?: folder.name,
                    artist = metadata?.artist ?: "",
                    tags = metadata?.tags ?: emptyList(),
                    thumbnailUrl = thumbnailUrl
                )
            }
    }

    private fun getThumbnailUrl(collectionId: String, collectionDir: File): String? {
        val thumbnailName = collectionDir.getThumbnailFileName() ?: return null
        return "/storage/images/$collectionId/$thumbnailName"
    }
    /**
     * 이미지 컬렉션 상세 정보를 조회한다.
     */
    fun getCollectionDetails(collectionId: String): ImageDetailsResponse? {
        val collectionDir = File(imagesDir, collectionId)
        if (!collectionDir.exists() || !collectionDir.isDirectory) return null

        val metadata = collectionDir.getMetaData()
        val imageNames = collectionDir.getImageFileNames()
        val imageUrls = imageNames.map { "/storage/images/$collectionId/$it" }
        val thumbnailUrl = getThumbnailUrl(collectionId, collectionDir) ?: return null

        return ImageDetailsResponse(
            id = collectionId,
            name = collectionId,
            title = metadata?.title ?: collectionId,
            artist = metadata?.artist ?: "",
            tags = metadata?.tags ?: emptyList(),
            description = metadata?.description ?: "",
            thumbnailUrl = thumbnailUrl,
            fileCount = imageNames.size,
            images = imageUrls
        )
    }

    fun createCollection(request: ImageUploadDto): ImageUploadResponse {
        return try {
            imagesDir.ensureExists()
            val collectionId = toSlug(request.title)
            val collectionDir = File(imagesDir, collectionId)

            if (!collectionDir.exists()) {
                collectionDir.mkdirs()
            }

            // 이미지 리스트 저장
            request.images.forEachIndexed { index, file ->
                if (!file.isEmpty) {
                    val extension = file.originalFilename?.substringAfterLast('.') ?: "jpg"
                    val fileName = String.format("%03d.%s", index + 1, extension)
                    val targetFile = File(collectionDir, fileName)
                    file.transferTo(targetFile)
                }
            }

            // 썸네일 저장
            request.thumbnail?.let { file ->
                if (!file.isEmpty) {
                    val extension = file.originalFilename?.substringAfterLast('.') ?: "jpg"
                    val thumbnailFile = File(collectionDir, "thumbnail.$extension")
                    file.transferTo(thumbnailFile)
                }
            }

            // metadata.json 저장
            val metadata = ImageMetadata(
                title = request.title,
                artist = request.artist,
                tags = request.tags,
                description = request.description ?: ""
            )
            collectionDir.saveMetaData(metadata)

            ImageUploadResponse(
                message = "컬렉션이 생성되었습니다. ID: $collectionId",
                status = true
            )
        } catch (e: Exception) {
            ImageUploadResponse(
                message = "컬렉션 생성 실패: ${e.message}",
                status = false
            )
        }
    }
    /**
     * 컬렉션을 삭제한다.
     */
    fun deleteCollection(collectionId: String): Boolean {
        val collectionDir = File(imagesDir, collectionId)

        if (!collectionDir.exists() || !collectionDir.isDirectory) {
            return false
        }

        return try {
            collectionDir.deleteRecursively()
            true
        } catch (e: Exception) {
            false
        }
    }
}