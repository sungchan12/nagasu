package com.mymedia.nagasu.dto;

import java.util.List;

/**
 * 이미지 컬렉션(폴더) 정보를 반환하는 응답 DTO
 */
public record ImageCollectionResponse(
        String id,
        String name,
        String title,
        String artist,
        List<String> tags,
        String thumbnailUrl,
        int viewCount
) {
}