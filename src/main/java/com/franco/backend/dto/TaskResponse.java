package com.franco.backend.dto;

import java.time.OffsetDateTime;

import com.franco.backend.entity.TaskStatus;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
