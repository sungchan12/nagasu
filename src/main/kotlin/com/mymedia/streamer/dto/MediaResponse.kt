package com.mymedia.streamer.dto
/**
 * 미디어 파일 정보를 클라이언트에게 전달하기 위한 응답 DTO
 */
data class MediaResponse(
    val id: String,        // 파일의 고유 ID (파일명)
    val name: String,      // 파일명
    val size: Long,        // 파일 크기 (바이트 단위)
    val type: String       // 미디어 타입: "VIDEO" 또는 "IMAGE"
)
