package com.mymedia.streamer.dto

data class VideoUploadResponseDto(
    val message: String,
    val status: Boolean,
    val collectionId: String? = null
)