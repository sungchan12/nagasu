package com.mymedia.nagasu.config;

import com.mymedia.nagasu.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.NoSuchFileException;
import java.util.NoSuchElementException;


@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoSuchFileException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(NoSuchFileException e) {
        logger.warn("File not found: {}", e.getMessage());
        var errorBody = new ApiResponse.Failure(e.getMessage() != null ? e.getMessage() : "File not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalStateException(IllegalStateException e) {
        logger.warn("Element not Found: {}", e.getMessage());
        var errorBody = new ApiResponse.Failure(e.getMessage() != null ? e.getMessage() :  "Element Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalStateException(IllegalStateException e) {
        logger.warn("StateException: {}", e.getMessage());
        var errorBody = new ApiResponse.Failure(e.getMessage() != null ? e.getMessage() : "An internal error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
    }
}
