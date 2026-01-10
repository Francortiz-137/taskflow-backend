package com.franco.backend.dto.auth;

import com.franco.backend.entity.UserRole;
import java.time.OffsetDateTime;

public record LoginResponse(
    Long id,
    String name,
    String email,
    UserRole role,
    OffsetDateTime createdAt,
    String accessToken,
    String refreshToken
) {}
