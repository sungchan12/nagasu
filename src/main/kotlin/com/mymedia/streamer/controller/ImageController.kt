package com.mymedia.streamer.controller

import com.mymedia.streamer.dto.ImageCollectionResponse
import com.mymedia.streamer.service.ImageService
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
     * 컬렉션의 썸네일 이미지를 반환한다.
     */
    @GetMapping("/{collectionId}/thumbnail")
    fun getThumbnail(@PathVariable collectionId: String): ResponseEntity<Resource> {
        val thumbnailFile = imageService.getThumbnailFile(collectionId)
            ?: return ResponseEntity.notFound().build()

        val resource = FileSystemResource(thumbnailFile)
        val contentType = Files.probeContentType(thumbnailFile.toPath())
            ?: "application/octet-stream"

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(resource)
    }
}