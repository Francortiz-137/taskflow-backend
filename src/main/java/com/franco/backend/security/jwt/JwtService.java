package com.franco.backend.security.jwt;

import java.util.Optional;

import com.franco.backend.entity.UserRole;

public interface JwtService {

    String generateToken(Long userId, String subject, UserRole role);

    boolean isValid(String token);

    Optional<String> extractSubject(String token);

    Optional<UserRole> extractRole(String token);

    Optional<Long> extractUserId(String token);
}
