package com.franco.backend.security.auth;

import com.franco.backend.entity.UserRole;

public record UserPrincipal(
    Long id,
    String email,
    UserRole role
) {}
