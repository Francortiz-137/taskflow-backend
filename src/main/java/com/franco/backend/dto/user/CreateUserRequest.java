package com.franco.backend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

    @NotBlank(message = "{validation.user.name.notBlank}")
    @Size(max = 150, message = "{validation.user.name.size}")
    String name,

    @NotBlank(message = "{validation.user.email.notBlank}")
    @Email(message = "{validation.user.email.invalid}")
    String email,

    @NotBlank(message = "{validation.user.password.notBlank}")
    @Size(min = 8, message = "{validation.user.password.size}")
    String password
) {}
