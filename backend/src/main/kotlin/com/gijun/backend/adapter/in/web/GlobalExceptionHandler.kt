package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.application.service.AuthenticationException
import com.gijun.backend.application.service.UserAlreadyExistsException
import com.gijun.backend.application.service.UserNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebInputException
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(e: AuthenticationException): ResponseEntity<ErrorResponse> {
        logger.warn("Authentication failed: ${e.message}")
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(
                success = false,
                message = e.message ?: "Authentication failed",
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleUserAlreadyExistsException(e: UserAlreadyExistsException): ResponseEntity<ErrorResponse> {
        logger.warn("User already exists: ${e.message}")
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(
                success = false,
                message = e.message ?: "User already exists",
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(e: UserNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("User not found: ${e.message}")
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                success = false,
                message = e.message ?: "User not found",
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = e.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        val message = "Validation failed: ${errors.joinToString(", ")}"
        
        logger.warn("Validation failed: $message")
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                success = false,
                message = message,
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleWebExchangeBindException(e: WebExchangeBindException): ResponseEntity<ErrorResponse> {
        val errors = e.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        val message = "Validation failed: ${errors.joinToString(", ")}"
        
        logger.warn("WebFlux validation failed: $message")
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                success = false,
                message = message,
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(ServerWebInputException::class)
    fun handleServerWebInputException(e: ServerWebInputException): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid input: ${e.message}")
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                success = false,
                message = e.message ?: "Invalid input",
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(e: ResponseStatusException): ResponseEntity<ErrorResponse> {
        logger.warn("Response status exception: ${e.message}")
        return ResponseEntity
            .status(e.statusCode)
            .body(ErrorResponse(
                success = false,
                message = e.reason ?: "Request failed",
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid argument: ${e.message}")
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                success = false,
                message = e.message ?: "Invalid argument",
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error occurred: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                success = false,
                message = "Internal server error",
                timestamp = LocalDateTime.now()
            ))
    }
}

data class ErrorResponse(
    val success: Boolean,
    val message: String,
    val timestamp: LocalDateTime
)
