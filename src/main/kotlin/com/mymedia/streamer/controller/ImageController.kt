package com.mymedia.streamer.controller

import com.mymedia.streamer.dto.ImageCollectionResponse
import com.mymedia.streamer.dto.ImageDetailsResponse
import com.mymedia.streamer.dto.ImageUploadDto
import com.mymedia.streamer.dto.ImageUploadResponse
import com.mymedia.streamer.service.ImageService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.awt.Image

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

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createCollection(
        @ModelAttribute requestDto: ImageUploadDto
    ): ResponseEntity<ImageUploadResponse> {
        // Service에 DTO 전체를 넘깁니다.
        val result = imageService.createCollection(requestDto)
        return ResponseEntity.ok(result)
    }
    /**
     * 컬렉션을 삭제한다.
     */
    @DeleteMapping("/{collectionId}")
    fun deleteCollection(@PathVariable collectionId: String): ResponseEntity<Map<String, Any>> {
        val success = imageService.deleteCollection(collectionId)

        return if (success) {
            ResponseEntity.ok(mapOf("success" to true, "message" to "Collection deleted successfully"))
        } else {
            ResponseEntity.notFound().build()
        }
    }
}