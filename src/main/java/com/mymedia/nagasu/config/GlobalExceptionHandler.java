package com.mymedia.nagasu.config;

import com.mymedia.nagasu.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.nio.file.NoSuchFileException;
import java.util.NoSuchElementException;


@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoSuchFileException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(NoSuchFileException e) {
        logger.warn("File not found: {}", e.getMessage());
        var errorBody = new ApiResponse.Failure<>(e.getMessage() != null ? e.getMessage() : "File not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<?>> handleNoSuchElement(NoSuchElementException e) {
        logger.warn("Element not Found: {}", e.getMessage());
        var errorBody = new ApiResponse.Failure<>(e.getMessage() != null ? e.getMessage() : "Element Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalStateException(IllegalStateException e) {
        logger.warn("StateException: {}", e.getMessage());
        var errorBody = new ApiResponse.Failure<>(e.getMessage() != null ? e.getMessage() : "An internal error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        logger.warn("MaxUploadSizeExceededException: {}", e.getMessage());
        var errorBody = new ApiResponse.Failure<>(e.getMessage() != null ? e.getMessage() : "Max upload size exceeded");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("IllegalArgumentException: {}", e.getMessage());
        var errorBody = new ApiResponse.Failure<>(e.getMessage() != null ? e.getMessage() : "IllegalArgumentException");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException e) {
        logger.warn("RuntimeException: {}", e.getMessage());
        var errorBody = new ApiResponse.Failure<>(e.getMessage() != null ? e.getMessage() : "A runtime error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
    }
}