package com.franco.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to create a new task")
public record CreateTaskRequest(

        @NotBlank
        @Schema(example = "Buy groceries")
        String title,

        @Schema(example = "Milk, Bread, Eggs")
        String description
) {}
