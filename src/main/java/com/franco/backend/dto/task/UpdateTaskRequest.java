package com.franco.backend.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateTaskRequest(
    @Schema(example = "Buy groceries")
    String title,

    @Schema(example = "Milk, Bread, Eggs")
    String description
) {}

