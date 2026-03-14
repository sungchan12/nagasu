/*
package com.mymedia.nagasu.controller

import com.mymedia.nagasu.dto.ApiResponse
import com.mymedia.nagasu.dto.VideoCollectionResponse
import com.mymedia.nagasu.dto.VideoDetailsResponse
import com.mymedia.nagasu.dto.VideoUploadRequestDto
import com.mymedia.nagasu.service.VideoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/ **
 * Video collection API controller
 * /
@RestController
@RequestMapping("/api/videos")
class VideoController(
    private val videoService: VideoService) {

    / **
     * Returns all video collections.
     * /
    @GetMapping
    fun getVideos(
        @RequestParam(defaultValue = "title") sort: String,
        @RequestParam(defaultValue = "asc") order: String
    ): List<VideoCollectionResponse> {
        return videoService.getVideoCollection(sort, order)
    }

    / **
     * Returns video collection details.
     * /
    @GetMapping("{id}/details")
    fun getVideoCollectionDetails(@PathVariable id: String): VideoDetailsResponse {
        return videoService.getVideoCollectionDetails(id)
    }

    / **
     * Uploads a new video collection.
     * /
    @PostMapping
    fun uploadVideoCollection(@ModelAttribute requestDto : VideoUploadRequestDto): ResponseEntity<ApiResponse<String>> {
        val collectionId = videoService.uploadVideoCollection(requestDto)
        return ResponseEntity.ok(ApiResponse.Success(collectionId))
    }

    / **
     * Deletes a video collection.
     * /
    @DeleteMapping("/{id}")
    fun deleteVideoCollection(@PathVariable id: String): ResponseEntity<ApiResponse<String>> {
        videoService.deleteVideoCollection(id)
        return ResponseEntity.ok(ApiResponse.Success("Video deleted successfully"))
    }

    / **
     * Repairs a manually added video collection (generates missing metadata, thumbnail, subtitles).
     * /
    @PostMapping("{id}/repair")
    fun repairVideoCollection(@PathVariable id: String): ResponseEntity<ApiResponse<String>> {
        videoService.repairVideoCollection(id)
        return ResponseEntity.ok(ApiResponse.Success("Video repaired successfully"))
    }
}
*/