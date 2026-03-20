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
        int viewCount,
        boolean isPrivate
) {
    public CollectionMetadata(String title) {
        this(title, "", List.of(), "", 0, false);
    }

    public CollectionMetadata(String title, String artist, List<String> tags, String description) {
        this(title, artist, tags, description, 0, false);
    }

    public CollectionMetadata(String title, String artist, List<String> tags, String description, int viewCount) {
        this(title, artist, tags, description, viewCount, false);
    }

    public CollectionMetadata viewCountIncrement() {
        return new CollectionMetadata(title, artist, tags, description, viewCount + 1, isPrivate);
    }

    public CollectionMetadata withPrivate(boolean isPrivate) {
        return new CollectionMetadata(title, artist, tags, description, viewCount, isPrivate);
    }
}