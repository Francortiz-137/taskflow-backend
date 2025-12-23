package com.franco.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

    @NotBlank(message = "{validation.user.email.notBlank}")
    @Email(message = "{validation.user.email.invalid}")
    String email,

    @NotBlank(message = "{validation.user.password.notBlank}")
    String password
) {}
