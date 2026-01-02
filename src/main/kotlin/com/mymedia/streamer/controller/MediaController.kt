package com.mymedia.streamer.controller

import com.mymedia.streamer.dto.MediaResponse
import com.mymedia.streamer.service.MediaService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 미디어 파일 API 컨트롤러
 */
@RestController
@RequestMapping("/api/media")
class MediaController(
    private val mediaService: MediaService
) {
    /**
     * 모든 미디어 파일 목록을 조회한다.
     * @return 미디어 파일 정보 리스트
     */
    @GetMapping
    fun getAllMedia(): List<MediaResponse> {
        return mediaService.getAllMedia()
    }

    /**
     * 이미지 파일 목록을 조회한다.
     */
    @GetMapping("/images")
    fun getImages(): List<MediaResponse> {
        return mediaService.getImages()
    }

    /**
     * 비디오 파일 목록을 조회한다.
     */
    @GetMapping("/videos")
    fun getVideos(): List<MediaResponse> {
        return mediaService.getVideos()
    }
}