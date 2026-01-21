package com.mymedia.nagasu.dto

data class ImageDetailsResponse(
    val id: String,
    val name: String,
    val title: String,
    val artist: String,
    val tags: List<String>,
    val description: String,
    val thumbnailUrl: String,
    val images: List<String>
)