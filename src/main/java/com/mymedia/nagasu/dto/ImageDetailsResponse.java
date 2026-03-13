package com.mymedia.nagasu.dto;

import java.util.List;

public record ImageDetailsResponse(
        String id,
        String name,
        String title,
        String artist,
        List<String> tags,
        String description,
        String thumbnailUrl,
        List<String> images
) {
}