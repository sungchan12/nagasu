package com.mymedia.streamer.service

import com.mymedia.streamer.dto.MediaResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File

/**
 * 미디어 파일 관리 서비스
 * 지정된 저장소 경로에서 미디어 파일 목록을 조회한다.
 */
@Service
class MediaService(
    @Value("\${storage.path}") private val storagePath: String
) {
    // 지원하는 비디오 확장자
    private val videoExtensions = setOf("mp4", "avi", "mkv", "mov")
    // 지원하는 이미지 확장자
    private val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "webp", "svg")

    // 하위 디렉토리 경로
    private val imagesDir: File get() = File(storagePath, "images")
    private val videosDir: File get() = File(storagePath, "videos")

    /**
     * 저장소의 모든 미디어 파일 목록을 조회한다.
     * @return 미디어 파일 정보 리스트
     */
    fun getAllMedia(): List<MediaResponse> {
        ensureDirectoriesExist()
        return getImages() + getVideos()
    }

    /**
     * 이미지 파일 목록을 조회한다.
     */
    fun getImages(): List<MediaResponse> {
        ensureDirectoriesExist()
        return getFilesFromDirectory(imagesDir, "IMAGE", imageExtensions)
    }

    /**
     * 비디오 파일 목록을 조회한다.
     */
    fun getVideos(): List<MediaResponse> {
        ensureDirectoriesExist()
        return getFilesFromDirectory(videosDir, "VIDEO", videoExtensions)
    }

    /**
     * 디렉토리가 없으면 생성한다.
     */
    private fun ensureDirectoriesExist() {
        if (!imagesDir.exists()) imagesDir.mkdirs()
        if (!videosDir.exists()) videosDir.mkdirs()
    }

    /**
     * 지정된 디렉토리에서 파일 목록을 조회한다.
     */
    private fun getFilesFromDirectory(
        directory: File,
        type: String,
        extensions: Set<String>
    ): List<MediaResponse> {
        return directory.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() in extensions }
            ?.map { file ->
                MediaResponse(
                    id = file.name,
                    name = file.name,
                    size = file.length(),
                    type = type
                )
            } ?: emptyList()
    }
}