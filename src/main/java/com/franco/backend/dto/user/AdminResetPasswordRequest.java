package com.franco.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminResetPasswordRequest(
    @NotBlank(message = "{validation.user.password.new.notBlank}")
    @Size(min = 8, message = "{validation.user.password.new.size}")
    String newPassword
) {}
