package com.mymedia.nagasu.dto;

public record VideoUploadResponseDto(
        String message,
        boolean status,
        String collectionId
) {
    public VideoUploadResponseDto(String message, boolean status) {
        this(message, status, null);
    }
}