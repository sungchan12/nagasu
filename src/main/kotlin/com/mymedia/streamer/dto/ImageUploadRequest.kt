package com.mymedia.streamer.dto

import java.io.File

data class ImageUploadRequest(
    val title: String,
    val artist: String,
    val tags: List<String>,
    val description: String?
)
