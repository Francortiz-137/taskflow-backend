package com.franco.backend.api;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.franco.backend.dto.common.ApiErrorResponse;
import com.franco.backend.exception.ApiException;
import com.franco.backend.exception.BadRequestException;
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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;


import lombok.extern.slf4j.Slf4j;


import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

@Slf4j
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
        Throwable cause = ex.getCause();

        // 🔒 Campo JSON desconocido (por FAIL_ON_UNKNOWN_PROPERTIES)
        if (cause instanceof UnrecognizedPropertyException upe) {
                String fieldName = upe.getPropertyName();

                String message = messageSource.getMessage(
                "validation.json.unknownField",
                new Object[]{fieldName},
                "Unknown field: " + fieldName,
                LocaleContextHolder.getLocale()
                );

                return buildValidationError(message, request);
        }

        // 🔒 Enum inválido (TaskStatus, etc.)
        if (cause instanceof InvalidFormatException ife
                && ife.getTargetType() != null
                && ife.getTargetType().isEnum()
        ) {
                String fieldName = ife.getPath().stream()
                        .map(ref -> ref.getFieldName())
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse("field");

                String message = messageSource.getMessage(
                "validation.invalidEnum",
                new Object[]{fieldName},
                fieldName + ": invalid value",
                LocaleContextHolder.getLocale()
                );

                return buildValidationError(message, request);
        }

        // 🔒 Fallback: JSON malformado
        String message = messageSource.getMessage(
                "validation.json.invalid",
                null,
                "Invalid request body",
                LocaleContextHolder.getLocale()
        );

        return buildValidationError(message, request);
        }

        // =========================
        // Method Argument Type Mismatch (e.g., Long id in path)
        // =========================
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
                MethodArgumentTypeMismatchException ex,
                HttpServletRequest request
        ) {
        String message = messageSource.getMessage(
                "validation.param.invalid",
                new Object[]{ex.getName()},
                ex.getName() + ": invalid value",
                LocaleContextHolder.getLocale()
        );

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
        // BadRequestException
        // =========================

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ApiErrorResponse> handleBadRequest(
                BadRequestException ex,
                HttpServletRequest request
        ) {
        String message = messageSource.getMessage(
                ex.getMessageKey(),
                ex.getArgs(),
                ex.getMessageKey(),
                LocaleContextHolder.getLocale()
        );

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




        private ResponseEntity<ApiErrorResponse> buildValidationError(
        String message,
        HttpServletRequest request
        ) {
        ApiErrorResponse response = new ApiErrorResponse(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.VALIDATION_ERROR.name(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
        }

        @ExceptionHandler({
        AccessDeniedException.class,
        AuthorizationDeniedException.class
        })
        public ResponseEntity<ApiErrorResponse> handleAccessDenied(
                Exception ex,
                HttpServletRequest request
        ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                new ApiErrorResponse(
                        OffsetDateTime.now(),
                        HttpStatus.FORBIDDEN.value(),
                        ErrorCode.FORBIDDEN.name(),
                        "FORBIDDEN",
                        request.getRequestURI()
                )
                );
        }

}
