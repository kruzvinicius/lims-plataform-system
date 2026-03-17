package com.kruzvinicius.limsbackend.dto.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

/**
 * Global Exception Handler for the LIMS Backend.
 * Intercepts specific exceptions and returns standardized error responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 404 - RESOURCE NOT FOUND
     * Handles both custom EntityNotFoundException and Jakarta/JPA standard exception.
     */
    @ExceptionHandler({
            com.kruzvinicius.limsbackend.dto.exception.EntityNotFoundException.class,
            jakarta.persistence.EntityNotFoundException.class
    })
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(Exception ex, WebRequest request) {
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false),
                "RESOURCE_NOT_FOUND"
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * 400 - BAD REQUEST (Bean Validation)
     * Handles @Valid errors in the Controller using Java 21's getFirst() method.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        // Java 21 syntax: .getFirst() replaces .get(0)
        String errorMessage = ex.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();

        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                "Validation Error: " + errorMessage,
                request.getDescription(false),
                "INVALID_ARGUMENTS"
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * 400 - BAD REQUEST (Illegal Arguments)
     * Handles cases where internal logic throws an IllegalArgumentException.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false),
                "BAD_REQUEST"
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * 500 - INTERNAL SERVER ERROR
     * Generic fallback to catch unhandled errors and log them for debugging.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        // Using 'ex' in the log to resolve the "parameter not used" warning
        log.error("A critical unexpected error occurred: ", ex);

        ErrorDetails error = new ErrorDetails(
                LocalDateTime.now(),
                "An unexpected internal server error occurred.",
                request.getDescription(false),
                "INTERNAL_SERVER_ERROR"
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}