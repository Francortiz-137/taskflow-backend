package com.franco.backend.dto;

import com.franco.backend.entity.TaskStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
    @NotNull(message = "status is required")
    TaskStatus status
) {}

