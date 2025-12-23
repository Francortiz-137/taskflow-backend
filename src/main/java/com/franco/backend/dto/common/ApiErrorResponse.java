package com.franco.backend.dto.common;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API error response")
public record ApiErrorResponse(
    @Schema(example = "2025-12-15T15:10:42.123Z")
    OffsetDateTime timestamp,

    @Schema(example = "404")
    int status,

    @Schema(example = "NOT_FOUND")
    String error,

    @Schema(example = "Task with id 99 not found")
    String message,

    @Schema(example = "/api/tasks/99")
    String path
) {}
