package com.franco.backend.api;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.franco.backend.dto.ApiErrorResponse;
import com.franco.backend.exception.ApiException;
import com.franco.backend.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;


import java.time.OffsetDateTime;
import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private final MessageSource messageSource;

        public GlobalExceptionHandler(MessageSource messageSource) {
                this.messageSource = messageSource;
        }

    // =========================
    // API Exceptions (negocio)
    // =========================
    @ExceptionHandler(ApiException.class)
        public ResponseEntity<ApiErrorResponse> handleApiException(
                ApiException ex,
                HttpServletRequest request
        ) {
        String message = messageSource.getMessage(
                ex.getMessageKey(),
                ex.getArgs(),
                ex.getMessageKey(), // fallback visible
                LocaleContextHolder.getLocale()
        );

        ApiErrorResponse response = new ApiErrorResponse(
                OffsetDateTime.now(),
                ex.getStatus().value(),
                ex.getErrorCode().name(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(ex.getStatus()).body(response);
        }


    // =========================
    // Bean Validation (DTOs)
    // =========================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
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
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.VALIDATION_ERROR.name(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    // =========================
    // Bean Validation (params/path)
    // =========================
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(v -> {
                    // si quieres incluir el campo: v.getPropertyPath() + ": " + v.getMessage()
                    return v.getMessage();
                })
                .findFirst()
                .orElse("Invalid request");

        ApiErrorResponse response = new ApiErrorResponse(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.VALIDATION_ERROR.name(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    // =========================
    // JSON parse errors / Enum inválido
    // =========================
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        String message = "Invalid request body";

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            // Caso enum inválido
            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                String fieldName = Optional.ofNullable(ife.getPath())
                        .flatMap(path -> path.stream()
                                .map(ref -> ref.getFieldName())
                                .filter(f -> f != null && !f.isBlank())
                                .findFirst())
                        .orElse("field");

                message = messageSource.getMessage(
                "validation.invalidEnum",
                new Object[]{fieldName},
                fieldName + ": invalid value",
                LocaleContextHolder.getLocale()
                );
            }
        }

        ApiErrorResponse response = new ApiErrorResponse(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.VALIDATION_ERROR.name(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }

    // =========================
    // Fallback 500 (controlado)
    // =========================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        String message = messageSource.getMessage(
        "error.internal",
        null,
        "Unexpected error",
        LocaleContextHolder.getLocale()
);

        ApiErrorResponse response = new ApiErrorResponse(
                OffsetDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ErrorCode.INTERNAL_ERROR.name(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

        // =========================
        // Optimistic Locking Failure
        // =========================

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
        public ResponseEntity<ApiErrorResponse> handleOptimisticLocking(
                ObjectOptimisticLockingFailureException ex,
                HttpServletRequest request
        ) {
        String message = messageSource.getMessage(
                "optimistic.lock",
                null,
                "The resource was modified by another request. Please retry.",
                LocaleContextHolder.getLocale()
        );    

        ApiErrorResponse response = new ApiErrorResponse(
            OffsetDateTime.now(),
            HttpStatus.CONFLICT.value(),
            ErrorCode.CONFLICT.name(),
            message,
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

}
