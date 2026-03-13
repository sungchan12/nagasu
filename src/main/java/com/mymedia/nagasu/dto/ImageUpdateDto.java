package com.mymedia.nagasu.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ImageUpdateDto(
        String title,
        String artist,
        List<String> tags,
        String description,
        MultipartFile thumbnail
) {
}