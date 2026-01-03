package com.mymedia.streamer.service

import com.mymedia.streamer.dto.ImageCollectionResponse
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

    /**
     * 이미지 컬렉션(폴더) 목록을 조회한다.
     */
    fun getCollections(): List<ImageCollectionResponse> {
        ensureDirectoryExists()

        return imagesDir.listFiles()
            ?.filter { it.isDirectory }
            ?.map { folder ->
                val imageFiles = countImageFiles(folder)
                ImageCollectionResponse(
                    id = folder.name,
                    name = folder.name,
                    thumbnailUrl = "/api/images/${folder.name}/thumbnail",
                    fileCount = imageFiles
                )
            }
            ?.filter { it.fileCount > 0 }  // 이미지가 있는 폴더만
            ?: emptyList()
    }

    /**
     * 컬렉션의 썸네일 이미지 파일을 반환한다.
     * 우선순위: thumbnail.* > 첫 번째 이미지
     */
    fun getThumbnailFile(collectionId: String): File? {
        val collectionDir = File(imagesDir, collectionId)
        if (!collectionDir.exists() || !collectionDir.isDirectory) return null

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
     * 폴더 내 이미지 파일 개수를 재귀적으로 카운트한다.
     */
    private fun countImageFiles(directory: File): Int {
        return directory.walkTopDown()
            .filter { it.isFile && it.extension.lowercase() in imageExtensions }
            .count()
    }

    private fun ensureDirectoryExists() {
        if (!imagesDir.exists()) imagesDir.mkdirs()
    }
}