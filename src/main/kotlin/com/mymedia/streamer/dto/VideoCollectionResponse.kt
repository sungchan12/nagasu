package com.mymedia.streamer.dto

/**
 * 비디오 컬렉션(폴더) 정보를 반환하는 응답 DTO
 */
data class VideoCollectionResponse(
    val id: String,
    val title: String,
    val artist: String,
    val tags: List<String>,
    val thumbnailUrl: String
)