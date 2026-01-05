package com.mymedia.streamer.dto

import org.springframework.web.multipart.MultipartFile

data class ImageUploadDto(
    val title: String,
    val artist: String,
    val tags: List<String> = emptyList(),
    val description: String?,
    val images: List<MultipartFile>,       // @RequestPart("images") 역할
    val thumbnail: MultipartFile? = null   // @RequestPart("thumbnail") 역할
)