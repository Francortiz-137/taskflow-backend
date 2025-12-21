package com.franco.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(
    @NotBlank(message = "Name must not be blank")
    String name,
    String email // solo para detectar intentos inválidos
) {}
