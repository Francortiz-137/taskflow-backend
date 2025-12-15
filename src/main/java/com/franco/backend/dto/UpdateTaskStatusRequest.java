package com.franco.backend.dto;

import com.franco.backend.entity.TaskStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateTaskStatusRequest(
    @Schema(
        allowableValues = {
            "TODO",
            "IN_PROGRESS",
            "DONE",
            "CANCELLED"
        }
    )
    TaskStatus status
) {}

