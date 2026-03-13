package com.mymedia.nagasu.dto;

import java.util.List;

/**
 * 비디오 컬렉션(폴더) 정보를 반환하는 응답 DTO
 */
public record VideoCollectionResponse(
        String id,
        String title,
        String artist,
        List<String> tags,
        String thumbnailUrl,
        int viewCount
) {
}