package com.mymedia.nagasu.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ImageUploadDto(
        String title,
        String artist,
        List<String> tags,
        String description,
        List<MultipartFile> images,
        MultipartFile thumbnail
) {
    public ImageUploadDto(String title, String artist, List<String> tags, String description,
                          List<MultipartFile> images) {
        this(title, artist, tags, description, images, null);
    }

    public ImageUploadDto(String title, String artist, String description,
                          List<MultipartFile> images) {
        this(title, artist, List.of(), description, images, null);
    }
}