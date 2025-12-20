package com.franco.backend.dto;

public record ChangePasswordRequest(
    String currentPassword,
    String newPassword
) {}
