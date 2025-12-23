package com.franco.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank(message = "{validation.user.password.current.notBlank}")
    String currentPassword,

    @NotBlank(message = "{validation.user.password.new.notBlank}")
    @Size(min = 8, message = "{validation.user.password.new.size}")
    String newPassword
) {}
