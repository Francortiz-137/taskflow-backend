package com.franco.backend.dto;

import com.franco.backend.entity.TaskStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request para crear o actualizar una tarea")
public record TaskRequest(
        @NotBlank
        @Schema(example = "Aprender Spring Boot")
        String title,

        @Schema(example = "Implementar CRUD profesional")
        String description,

        @Schema(
        description = "Estado de la tarea",
        allowableValues = {
            "TODO",
            "IN_PROGRESS",
            "DONE",
            "CANCELLED"
        },
        example = "TODO"
        )
        TaskStatus status
) {}