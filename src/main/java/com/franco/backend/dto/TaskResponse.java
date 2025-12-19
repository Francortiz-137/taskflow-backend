package com.franco.backend.dto;

import java.time.OffsetDateTime;

import com.franco.backend.entity.TaskStatus;

import io.swagger.v3.oas.annotations.media.Schema;


public record TaskResponse(
        @Schema(description = "Unique ID", example = "1")
        Long id,
        @Schema(description = "Title of the task", example = "Buy groceries")
        String title,
        @Schema(description = "Description of the task", example = "Milk, Bread, Eggs")
        String description,
        @Schema(description = "Status of the task", example = "TODO")
        TaskStatus status,
        @Schema(description = "Creation date of the task", example = "2023-05-15T10:00:00Z")
        OffsetDateTime createdAt,
        @Schema(description = "Update date of the task", example = "2023-05-15T11:30:00Z")
        OffsetDateTime updatedAt
) {}
