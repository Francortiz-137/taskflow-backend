package com.franco.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request para crear una nueva tarea")
public record CreateTaskRequest(

        @NotBlank
        @Schema(example = "Aprender Spring Boot")
        String title,

        @Schema(example = "Implementar CRUD profesional")
        String description
) {}
