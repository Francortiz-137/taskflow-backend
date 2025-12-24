package com.franco.backend.security.jwt;

import java.util.Optional;

public interface JwtService {

    String generateToken(String subject);

    boolean isValid(String token);

    Optional<String> extractSubject(String token);
}
