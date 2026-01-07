package com.mymedia.streamer.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mymedia.streamer.dto.ImageCollectionResponse
import com.mymedia.streamer.dto.ImageDetailsResponse
import com.mymedia.streamer.dto.ImageUploadDto
import com.mymedia.streamer.dto.ImageUploadResponse
import com.mymedia.streamer.dto.metadata.ImageMetadata
import com.mymedia.streamer.utils.toSlug
import com.mymedia.streamer.utils.ensureExists
import com.mymedia.streamer.repository.getCollectionDirs
import com.mymedia.streamer.repository.getThumbnailImageFile
import com.mymedia.streamer.repository.getImageFiles
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
    private val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "webp", "svg")
    private val imagesDir: File get() = File(storagePath, "images")
    private val objectMapper = jacksonObjectMapper()

    /**
     * 이미지 컬렉션(폴더) 목록을 조회한다.
     */
    fun getCollections(): List<ImageCollectionResponse> {
        imagesDir.ensureExists()

        return imagesDir.getCollectionDirs()
            .mapNotNull { folder ->
                val thumbnailUrl = getThumbnailUrl(folder.name, folder) ?: return@mapNotNull null
                val metadata = loadMetadata(folder)

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

    /**
     * 컬렉션의 썸네일 이미지 파일을 반환한다.
     * 우선순위: thumbnail.* > 첫 번째 이미지
     */
    private fun getThumbnailFile(collectionDir: File): File? {
        // 1. thumbnail.* 파일 찾기
        val thumbnailFile = collectionDir.listFiles()
            ?.find { it.isFile && it.nameWithoutExtension.lowercase() == "thumbnail"
                    && it.extension.lowercase() in imageExtensions }

        if (thumbnailFile != null) return thumbnailFile

        // 2. 첫 번째 이미지 파일 사용
        return collectionDir.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() in imageExtensions }
            ?.minByOrNull { it.name }
    }

    /**
     * 컬렉션의 썸네일 URL을 반환한다.
     */
    private fun getThumbnailUrl(collectionId: String, collectionDir: File): String? {
        val thumbnailFile = getThumbnailFile(collectionDir) ?: return null
        return "/storage/images/$collectionId/${thumbnailFile.name}"
    }

    /**
     * 폴더 내 이미지 파일 개수를 재귀적으로 카운트한다.
     */
    private fun countImageFiles(directory: File): Int {
        return directory.walkTopDown().count { it.isFile && it.extension.lowercase() in imageExtensions }
    }
    /**
     * 이미지 컬렉션 상세 정보를 조회한다.
     */
    fun getCollectionDetails(collectionId: String): ImageDetailsResponse? {
        val collectionDir = File(imagesDir, collectionId)
        if (!collectionDir.exists() || !collectionDir.isDirectory) return null

        val metadata = loadMetadata(collectionDir)
        val imageFiles = getImageFiles(collectionDir)
        val imageUrls = imageFiles.map { "/storage/images/$collectionId/${it.name}" }
        val thumbnailUrl = getThumbnailUrl(collectionId, collectionDir) ?: return null

        return ImageDetailsResponse(
            id = collectionId,
            name = collectionId,
            title = metadata?.title ?: collectionId,
            artist = metadata?.artist ?: "",
            tags = metadata?.tags ?: emptyList(),
            description = metadata?.description ?: "",
            thumbnailUrl = thumbnailUrl,
            fileCount = imageFiles.size,
            images = imageUrls
        )
    }

    /**
     * 폴더 내 이미지 파일 목록을 반환한다.
     */
    private fun getImageFiles(directory: File): List<File> {
        return directory.walkTopDown()
            .filter { it.isFile && it.extension.lowercase() in imageExtensions }
            .sortedBy { it.name }
            .toList()
    }

    /**
     * 컬렉션 폴더에서 metadata.json을 읽어온다.
     */
    private fun loadMetadata(collectionDir: File): ImageMetadata? {
        val metadataFile = File(collectionDir, "metadata.json")
        if (!metadataFile.exists()) return null

        return try {
            objectMapper.readValue<ImageMetadata>(metadataFile)
        } catch (e: Exception) {
            null
        }
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
            val metadataFile = File(collectionDir, "metadata.json")
            objectMapper.writeValue(metadataFile, metadata)

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