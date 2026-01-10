package com.mymedia.streamer.controller

import com.mymedia.streamer.dto.VideoCollectionResponse
import com.mymedia.streamer.service.VideoService
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Files

/**
 * 비디오 컬렉션 API 컨트롤러
 */
@RestController
@RequestMapping("/api/videos")
class VideoController(
    private val videoService: VideoService
) {
    @GetMapping
    fun getVideos(): List<VideoCollectionResponse> {
        return videoService.getVideoCollection()
    }
}