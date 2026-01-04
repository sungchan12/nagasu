package com.mymedia.streamer.dto

/**
 * 이미지 컬렉션(폴더) 정보를 반환하는 응답 DTO
 */
data class ImageCollectionResponse(
    val id: String,
    val name: String,
    val title: String,
    val artist: String,
    val tags: List<String>,
    val thumbnailUrl: String
)