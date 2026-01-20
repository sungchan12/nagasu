package com.mymedia.streamer.service

import com.mymedia.streamer.dto.VideoCollectionResponse
import com.mymedia.streamer.dto.VideoDetailsResponse
import com.mymedia.streamer.dto.VideoUploadRequestDto
import com.mymedia.streamer.dto.VideoUploadResponseDto
import com.mymedia.streamer.dto.metadata.CollectionMetadata
import com.mymedia.streamer.repository.getSubtitleFileName
import com.mymedia.streamer.repository.getThumbnailFileName
import com.mymedia.streamer.repository.getVideoCollection
import com.mymedia.streamer.repository.getVideoFileName
import com.mymedia.streamer.utils.ensureExists
import com.mymedia.streamer.utils.extractThumbnailToFolder
import com.mymedia.streamer.utils.getMetaData
import com.mymedia.streamer.utils.saveMetaData
import com.mymedia.streamer.utils.toSlug
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File

/**
 * 비디오 컬렉션 관리 서비스
 */
@Service
class VideoService(
    @Value("\${storage.path}") private val storagePath: String) {
    private val videoDir = File(storagePath, "videos")
    fun getVideoCollection(): List<VideoCollectionResponse> {
        videoDir.ensureExists()
        return videoDir.getVideoCollection()
            .mapNotNull { folderName ->
                val folder = File(videoDir, folderName)
                val thumbnailUrl = getThumbnailUrl(folder) ?: return@mapNotNull null
                val metadata = folder.getMetaData()

                VideoCollectionResponse(
                    id = folderName,
                    title = metadata?.title ?: folderName,
                    artist = metadata?.artist ?: "",
                    tags = metadata?.tags ?: emptyList(),
                    thumbnailUrl = thumbnailUrl
                )
            }
    }

    fun getVideoCollectionDetails(collectionId: String): VideoDetailsResponse? {
        val videoColDir = File(videoDir, collectionId)
        videoDir.ensureExists()
        val metadata = videoColDir.getMetaData()
        val thumbnailUrl = getThumbnailUrl(videoColDir) ?: return null
        val videoUrl = getVideoUrl(videoColDir) ?: return null
        val videoSubtitleUrl = getVideoSubtitle(videoColDir) ?: return null
        return VideoDetailsResponse(
            id = collectionId,
            name = videoColDir.name,
            title = metadata.title,
            artist = metadata.artist,
            tags = metadata.tags,
            description = metadata.description,
            thumbnailUrl = thumbnailUrl,
            videoUrl = videoUrl,
            videoSubtitleUrl = videoSubtitleUrl
        )
    }

    private fun getThumbnailUrl(collectionDir: File): String? {
        val thumbnailName = collectionDir.getThumbnailFileName() ?: return null
        return "/storage/videos/${collectionDir.name}/$thumbnailName"
    }

    private fun getVideoUrl(collectionDir: File): String? {
        val videoFileName = collectionDir.getVideoFileName() ?: return null
        return "/storage/videos/${collectionDir.name}/$videoFileName"
    }

    private fun getVideoSubtitle(collectionDir: File): String? {
        val videoSubtitle = collectionDir.getSubtitleFileName()
        return "/storage/videos/${collectionDir.name}/$videoSubtitle"
    }

    fun uploadVideoCollection(videoUploadRequestDto: VideoUploadRequestDto): VideoUploadResponseDto {
        return try {
            videoDir.ensureExists()
            val collectionId = toSlug(videoUploadRequestDto.title)
            val collectionDir = File(videoDir, collectionId)
            collectionDir.ensureExists()

            // 비디오 저장
            val originalFileName = videoUploadRequestDto.video.originalFilename ?: "video.mp4"
            val videoFile = File(collectionDir, originalFileName)
            videoUploadRequestDto.video.transferTo(videoFile)

            // 썸네일 처리
            val thumbnailInput = videoUploadRequestDto.thumbnail
            if (thumbnailInput != null && !thumbnailInput.isEmpty) {
                // 사용자가 제공한 썸네일 저장
                val thumbnailFileName = thumbnailInput.originalFilename ?: "thumbnail.jpg"
                val thumbnailFile = File(collectionDir, thumbnailFileName)
                thumbnailInput.transferTo(thumbnailFile)
            } else {
                // FFmpeg으로 비디오에서 썸네일 자동 생성
                extractThumbnailToFolder(videoFile)
            }
            // 자막 저장
            val subtitleInput = videoUploadRequestDto.subtitle
            if (subtitleInput != null && !subtitleInput.isEmpty) {
                val videoBaseName = videoUploadRequestDto.video.originalFilename?.substringBeforeLast('.') ?: "video"
                val subtitleExtension = subtitleInput.originalFilename?.substringAfterLast('.', "srt") ?: "srt"
                val subtitleFileName = "$videoBaseName.$subtitleExtension"
                val subtitleFile = File(collectionDir, subtitleFileName)
                subtitleInput.transferTo(subtitleFile)
            }
            // 메타데이터 저장
            val metadata = CollectionMetadata(
                title = videoUploadRequestDto.title,
                artist = videoUploadRequestDto.artist,
                tags = videoUploadRequestDto.tags,
                description = videoUploadRequestDto.description ?: ""
            )
            collectionDir.saveMetaData(metadata)

            VideoUploadResponseDto(
                message = "동영상 업로드 성공",
                status = true,
                collectionId = collectionId
            )
        } catch (e: Exception) {
            VideoUploadResponseDto(
                message = "동영상 업로드 실패: ${e.message}",
                status = false
            )
        }
    }

    fun deleteVideoCollection(collectionId: String): Boolean {
        try {
            val collectionDir = File(videoDir, collectionId)
            if (!collectionDir.exists() || !collectionDir.isDirectory) {
                return false
            }
            return collectionDir.deleteRecursively()
        } catch (e: Exception) {
            return false
        }
    }
}