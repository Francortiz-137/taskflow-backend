package com.franco.backend.config;

import com.franco.backend.dto.ApiErrorResponse;
import com.franco.backend.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(
            ApiException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                OffsetDateTime.now(),
                ex.getStatus(),
                ex.getError(),
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");

        ApiErrorResponse response = new ApiErrorResponse(
                OffsetDateTime.now(),
                400,
                "VALIDATION_ERROR",
                message,
                request.getRequestURI());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
                ConstraintViolationException ex,
                HttpServletRequest request
        ) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getMessage())
                .findFirst()
                .orElse("Invalid request");

        ApiErrorResponse response = new ApiErrorResponse(
                OffsetDateTime.now(),
                400,
                "VALIDATION_ERROR",
                message,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
        }
}
