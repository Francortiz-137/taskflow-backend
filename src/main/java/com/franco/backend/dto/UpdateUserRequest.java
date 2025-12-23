package com.franco.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @NotBlank(message = "{validation.user.name.notBlank}")
    @Size(max = 150, message = "{validation.user.name.size}")
    String name
) {}
