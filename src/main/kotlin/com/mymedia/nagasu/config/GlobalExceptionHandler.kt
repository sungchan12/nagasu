package com.mymedia.nagasu.config

import com.mymedia.nagasu.dto.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
import java.nio.file.NoSuchFileException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(NoSuchFileException::class)
    fun handleNotFound(e: NoSuchFileException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("File not found: ${e.message}")
        val errorBody = ApiResponse.Failure(e.message ?: "File not found")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElement(e: NoSuchElementException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Element not found: ${e.message}")
        val errorBody = ApiResponse.Failure(e.message ?: "Element not found")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(e: IllegalStateException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("StateException: ${e.message}")
        val errorBody = ApiResponse.Failure(e.message ?: "An internal error occurred")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody)
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSizeExceededException(e: MaxUploadSizeExceededException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("MaxUploadSizeExceededException: ${e.message}")
        val errorBody = ApiResponse.Failure(e.message ?: "Max upload size exceeded")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("IllegalArgumentException: ${e.message}")
        val errorBody = ApiResponse.Failure(e.message ?: "IllegalArgumentException")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody)
    }
}