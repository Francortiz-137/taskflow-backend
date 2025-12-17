package com.franco.backend.config;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.franco.backend.dto.ApiErrorResponse;
import com.franco.backend.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

        //Errores personalizados de la API
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

        //Error de validación en request bodies
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

        // Error debido a violaciones de constraints en path variables o request params
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


        //Error debido a formato inválido en el body (enum inválido)
        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
                HttpMessageNotReadableException ex,
                HttpServletRequest request
        ) {
        String message = "Invalid request body";

        if (ex.getCause() instanceof InvalidFormatException ife) {
                if (ife.getTargetType().isEnum()) {
                String fieldName = ife.getPath().stream()
                        .map(ref -> ref.getFieldName())
                        .findFirst()
                        .orElse("field");

                message = fieldName + ": invalid value";
                }
        }

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
