package com.franco.backend.dto.task;

import jakarta.validation.constraints.NotBlank;

public record UpdateTaskRequest(
       @NotBlank(message = "Title must not be blank")
        String title,
        String description
) {}
