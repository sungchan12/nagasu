package com.mymedia.streamer.controller

import com.mymedia.streamer.dto.ImageCollectionResponse
import com.mymedia.streamer.dto.ImageDetailsResponse
import com.mymedia.streamer.service.ImageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 이미지 컬렉션 API 컨트롤러
 */
@RestController
@RequestMapping("/api/images")
class ImageController(
    private val imageService: ImageService
) {
    /**
     * 이미지 컬렉션 목록을 조회한다.
     */
    @GetMapping
    fun getCollections(): List<ImageCollectionResponse> {
        return imageService.getCollections()
    }

    /**
     * 컬렉션 상세 정보를 조회한다.
     */
    @GetMapping("/{collectionId}/details")
    fun getCollectionDetails(@PathVariable collectionId: String): ResponseEntity<ImageDetailsResponse> {
        val details = imageService.getCollectionDetails(collectionId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(details)
    }
}