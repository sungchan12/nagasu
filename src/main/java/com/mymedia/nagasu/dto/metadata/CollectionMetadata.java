package com.mymedia.nagasu.dto.metadata;

import java.util.List;

/**
 * metadata.json 파싱용 데이터 클래스
 */
public record CollectionMetadata(
        String title,
        String artist,
        List<String> tags,
        String description,
        int viewCount
) {
    public CollectionMetadata(String title) {
        this(title, "", List.of(), "", 0);
    }

    public CollectionMetadata(String title, String artist) {
        this(title, artist, List.of(), "", 0);
    }
}