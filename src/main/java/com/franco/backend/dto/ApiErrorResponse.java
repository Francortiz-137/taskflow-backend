package com.franco.backend.dto;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Respuesta estándar de error")
@Builder
public class ApiErrorResponse {

    @Schema(example = "2025-12-15T15:10:42.123Z")
    private OffsetDateTime timestamp;

    @Schema(example = "404")
    private int status;

    @Schema(example = "NOT_FOUND")
    private String error;

    @Schema(example = "Task with id 99 not found")
    private String message;

    @Schema(example = "/api/tasks/99")
    private String path;
}
