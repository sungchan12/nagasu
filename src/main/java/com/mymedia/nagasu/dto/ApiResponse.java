package com.mymedia.nagasu.dto;

public sealed interface ApiResponse<T> permits ApiResponse.Success, ApiResponse.Failure {

    record Success<T>(T data) implements ApiResponse<T> {
    }

    record Failure<T>(String error) implements ApiResponse<T> {
    }
}