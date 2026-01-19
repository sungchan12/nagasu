package com.mymedia.streamer.dto

data class VideoDetailsResponse(
    val id: String,
    val name: String,
    val title: String,
    val artist: String,
    val tags: List<String>,
    val description: String,
    val thumbnailUrl: String,
    val videoUrl: String,
)
