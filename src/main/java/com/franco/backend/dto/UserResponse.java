package com.franco.backend.dto;

import com.franco.backend.entity.UserRole;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

public record UserResponse(
    @Schema(description = "Unique ID", example = "1")
    Long id,
    @Schema(description = "Name of the user", example = "John Doe")
    String name,
    @Schema(description = "Email of the user", example = "john.doe@example.com")
    String email,
    @Schema(description = "Role of the user", example = "USER")
    UserRole role,
    @Schema(description = "Creation date of the user", example = "2023-05-15T10:00:00Z")
    OffsetDateTime createdAt,
    @Schema(description = "Update date of the user", example = "2023-05-15T11:30:00Z")
    OffsetDateTime updatedAt
) {}
