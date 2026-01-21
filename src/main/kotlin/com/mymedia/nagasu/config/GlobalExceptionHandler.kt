package com.mymedia.nagasu.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

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

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(e: IllegalStateException): ResponseEntity<Map<String, String>> {
        logger.warn("StateException: ${e.message}")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf(
            "error" to "Internal Server Error",
            "message" to (e.message ?: "An internal error occurred")
        ))
    }
}