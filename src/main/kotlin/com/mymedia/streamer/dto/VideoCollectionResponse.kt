package com.mymedia.streamer.dto

/**
 * 비디오 컬렉션(폴더) 정보를 반환하는 응답 DTO
 */
data class VideoCollectionResponse(
    val id: String,           // 컬렉션 ID (폴더 경로)
    val name: String,         // 컬렉션 이름 (폴더명)
    val thumbnailUrl: String, // 썸네일 URL
    val fileCount: Int        // 비디오 파일 개수
)