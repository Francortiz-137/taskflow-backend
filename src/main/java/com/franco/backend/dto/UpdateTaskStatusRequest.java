package com.franco.backend.dto;

import com.franco.backend.entity.TaskStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
    @NotNull(message = "status is required")
    TaskStatus status
) {}

