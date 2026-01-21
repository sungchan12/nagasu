package com.mymedia.nagasu.controller

import com.mymedia.nagasu.dto.VideoCollectionResponse
import com.mymedia.nagasu.dto.VideoDetailsResponse
import com.mymedia.nagasu.dto.VideoUploadRequestDto
import com.mymedia.nagasu.dto.VideoUploadResponseDto
import com.mymedia.nagasu.service.VideoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 비디오 컬렉션 API 컨트롤러
 */
@RestController
@RequestMapping("/api/videos")
class VideoController(
    private val videoService: VideoService) {
    @GetMapping
    fun getVideos(): List<VideoCollectionResponse> {
        return videoService.getVideoCollection()
    }

    @GetMapping("{id}/details")
    fun getVideoCollectionDetails(@PathVariable id: String): ResponseEntity<VideoDetailsResponse> {
        val result = videoService.getVideoCollectionDetails(id)
        return ResponseEntity.ok(result)
    }

    @PostMapping
    fun uploadVideoCollection(@ModelAttribute requestDto : VideoUploadRequestDto): ResponseEntity<VideoUploadResponseDto> {
        val result = videoService.uploadVideoCollection(requestDto)
        return ResponseEntity.ok(result)
    }

    @DeleteMapping("/{id}")
    fun deleteVideoCollection(@PathVariable id: String): ResponseEntity<Map<String, Any>> {
        videoService.deleteVideoCollection(id)
        return ResponseEntity.ok(mapOf("success" to true, "message" to "Video deleted successfully"))
    }
}