package com.mymedia.nagasu.dto;

import java.util.List;

public record VideoDetailsResponse(
        String id,
        String name,
        String title,
        String artist,
        List<String> tags,
        String description,
        String thumbnailUrl,
        String videoUrl,
        String videoSubtitleUrl,
        int viewCount
) {
}