package com.mymedia.streamer.service

import com.mymedia.streamer.dto.VideoCollectionResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File

/**
 * 비디오 컬렉션 관리 서비스
 */
@Service
class VideoService(
    @Value("\${storage.path}") private val storagePath: String
) {
    private val videoExtensions = setOf("mp4", "avi", "mkv", "mov", "webm")
    private val videosDir: File get() = File(storagePath, "videos")

    /**
     * 비디오 컬렉션(폴더) 목록을 조회한다.
     */
    fun getCollections(): List<VideoCollectionResponse> {
        ensureDirectoryExists()

        return videosDir.listFiles()
            ?.filter { it.isDirectory }
            ?.map { folder ->
                val videoFiles = countVideoFiles(folder)
                VideoCollectionResponse(
                    id = folder.name,
                    name = folder.name,
                    thumbnailUrl = "/api/videos/${folder.name}/thumbnail",
                    fileCount = videoFiles
                )
            }
            ?.filter { it.fileCount > 0 }  // 비디오가 있는 폴더만
            ?: emptyList()
    }

    /**
     * 컬렉션의 썸네일 이미지 파일을 반환한다.
     * 우선순위: thumbnail.* > 폴더 내 첫 번째 이미지
     * TODO: 이미지가 없으면 영상 프레임 추출 (ffmpeg 필요)
     */
    fun getThumbnailFile(collectionId: String): File? {
        val collectionDir = File(videosDir, collectionId)
        if (!collectionDir.exists() || !collectionDir.isDirectory) return null

        // 1. thumbnail.* 파일 찾기
        val thumbnailFile = collectionDir.listFiles()
            ?.find { it.isFile && it.nameWithoutExtension.lowercase() == "thumbnail"
                    && it.extension.lowercase() in imageExtensions }

        if (thumbnailFile != null) return thumbnailFile

        // 2. 폴더 내 첫 번째 이미지 파일 사용
        return collectionDir.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() in imageExtensions }
            ?.minByOrNull { it.name }
    }

    /**
     * 폴더 내 비디오 파일 개수를 재귀적으로 카운트한다.
     */
    private fun countVideoFiles(directory: File): Int {
        return directory.walkTopDown()
            .filter { it.isFile && it.extension.lowercase() in videoExtensions }
            .count()
    }

    private fun ensureDirectoryExists() {
        if (!videosDir.exists()) videosDir.mkdirs()
    }
}