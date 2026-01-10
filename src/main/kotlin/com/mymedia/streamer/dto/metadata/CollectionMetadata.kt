package com.mymedia.streamer.dto.metadata

/**
 * metadata.json 파싱용 데이터 클래스
 */
data class CollectionMetadata(
    val title: String,
    val artist: String = "",
    val tags: List<String> = emptyList(),
    val description: String = ""
)