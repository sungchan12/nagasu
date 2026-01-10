package com.mymedia.streamer.service

import com.mymedia.streamer.dto.VideoCollectionResponse
import com.mymedia.streamer.repository.getThumbnailFileName
import com.mymedia.streamer.repository.getVideoCollection
import com.mymedia.streamer.utils.ensureExists
import com.mymedia.streamer.utils.getMetaData
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
    private val videoDir = File(storagePath, "videos")
    fun getVideoCollection(): List<VideoCollectionResponse> {
        videoDir.ensureExists()
        return videoDir.getVideoCollection()
            .mapNotNull { folder ->
                val thumbnailUrl = getThumbnailUrl(folder.name, folder) ?: return@mapNotNull null
                val metadata = folder.getMetaData()
                VideoCollectionResponse(
                    id = folder.name,
                    title = metadata?.title ?: folder.name,
                    artist = metadata?.artist ?: "",
                    tags = metadata?.tags ?: emptyList(),
                    thumbnailUrl = thumbnailUrl
                )
            }
    }
    private fun getThumbnailUrl(collectionId: String, collectionDir: File): String? {
        val thumbnailName = collectionDir.getThumbnailFileName() ?: return null
        return "/storage/videos/$collectionId/$thumbnailName"
    }
}