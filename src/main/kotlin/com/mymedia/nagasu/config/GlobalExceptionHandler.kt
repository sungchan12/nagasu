package com.mymedia.nagasu.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(NoSuchFileException::class)
    fun handleNotFound(e: NoSuchFileException): ResponseEntity<Map<String, String>> {
        logger.warn("File not found: ${e.message}")
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(mapOf(
                "error" to "Not Found",
                "message" to (e.message ?: "there is no such file")
            ))
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElement(e: NoSuchElementException): ResponseEntity<Map<String, String>> {
        logger.warn("Element not found: ${e.message}")
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(mapOf(
                "error" to "Not Found",
                "message" to (e.message ?: "Request resource not found")
            ))
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(e: IllegalStateException): ResponseEntity<Map<String, String>> {
        logger.warn("StateException: ${e.message}")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
            "error" to "Internal Server Error",
            "message" to (e.message ?: "An internal error occurred")
        ))
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSizeExceededException(e: MaxUploadSizeExceededException): ResponseEntity<Map<String, String>> {
        logger.warn("MaxUploadSizeExceededException: ${e.message}")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
            "error" to "Max Upload Size Exceeded",
            "message" to (e.message ?: "An internal error occurred")
        ))
    }
}