package com.mymedia.nagasu.dto

import org.springframework.web.multipart.MultipartFile

data class ImageUpdateDto(
    val title: String? = null,
    val artist: String? = null,
    val tags: List<String>? = null,
    val description: String? = null,
    val thumbnail: MultipartFile? = null
)
