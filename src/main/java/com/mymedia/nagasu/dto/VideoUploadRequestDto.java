package com.mymedia.nagasu.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record VideoUploadRequestDto(
        String title,
        String artist,
        List<String> tags,
        String description,
        MultipartFile video,
        MultipartFile thumbnail,
        MultipartFile subtitle
) {
    public VideoUploadRequestDto(String title, String artist, List<String> tags, String description,
                                 MultipartFile video) {
        this(title, artist, tags, description, video, null, null);
    }

    public VideoUploadRequestDto(String title, String artist, String description,
                                 MultipartFile video) {
        this(title, artist, List.of(), description, video, null, null);
    }
}