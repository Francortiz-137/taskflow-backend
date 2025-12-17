package com.franco.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateTaskRequest(
       @NotBlank(message = "title must not be blank")
        String title,
        String description
) {}
