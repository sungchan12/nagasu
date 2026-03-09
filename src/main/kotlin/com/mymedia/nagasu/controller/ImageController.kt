package com.mymedia.nagasu.controller

import com.mymedia.nagasu.dto.ApiResponse
import com.mymedia.nagasu.dto.ImageCollectionResponse
import com.mymedia.nagasu.dto.ImageDetailsResponse
import com.mymedia.nagasu.dto.ImageUploadDto
import com.mymedia.nagasu.service.ImageService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Image collection API controller
 */
@RestController
@RequestMapping("/api/images")
class ImageController(
    private val imageService: ImageService
) {
    /**
     * Returns all image collections.
     */
    @GetMapping
    fun getCollections(
        @RequestParam(defaultValue = "title") sort: String,
        @RequestParam(defaultValue = "view") order: String
    ): List<ImageCollectionResponse> {
        return imageService.getCollections(sort, order)
    }

    /**
     * Returns collection details.
     */
    @GetMapping("/{collectionId}/details")
    fun getCollectionDetails(@PathVariable collectionId: String): ResponseEntity<ImageDetailsResponse> {
        val details = imageService.getCollectionDetails(collectionId)
        return ResponseEntity.ok(details)
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createCollection(@ModelAttribute requestDto: ImageUploadDto): ResponseEntity<ApiResponse<String>> {
        val result = imageService.createCollection(requestDto)
        return ResponseEntity.ok(ApiResponse.Success(result))
    }
    /**
     * Deletes a collection.
     */
    @DeleteMapping("/{collectionId}")
    fun deleteCollection(@PathVariable collectionId: String): ResponseEntity<ApiResponse<String>> {
        imageService.deleteCollection(collectionId)
        return ResponseEntity.ok(ApiResponse.Success("Collection deleted Successfully!"))
    }
}