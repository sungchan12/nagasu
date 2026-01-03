package com.mymedia.streamer.dto

/**
 * 이미지 컬렉션(폴더) 정보를 반환하는 응답 DTO
 */
data class ImageCollectionResponse(
    val id: String,           // 컬렉션 ID (폴더 경로)
    val name: String,         // 컬렉션 이름 (폴더명)
    val thumbnailUrl: String, // 썸네일 URL
    val fileCount: Int        // 이미지 파일 개수
)