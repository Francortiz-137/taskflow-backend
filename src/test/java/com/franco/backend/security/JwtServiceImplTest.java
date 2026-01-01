package com.franco.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.franco.backend.config.JwtProperties;
import com.franco.backend.security.jwt.JwtService;
import com.franco.backend.security.jwt.impl.JwtServiceImpl;
import com.franco.backend.entity.UserRole;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    private JwtService jwtService;

    private final JwtProperties properties = new JwtProperties(
        "test-secret-test-secret-test-secret-test-secret",
        3600
    );

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl(properties);
    }

     @Test
    void shouldGenerateAndParseToken() {
        String token = jwtService.generateToken(
            1L,
            "user@test.com",
            UserRole.USER
        );

        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractSubject(token)).contains("user@test.com");
        assertThat(jwtService.extractRole(token)).contains(UserRole.USER);
        assertThat(jwtService.extractUserId(token)).contains(1L);
    }

    @Test
    void shouldReturnEmptyWhenTokenIsInvalid() {
        String invalid = "invalid.token.here";

        assertThat(jwtService.isValid(invalid)).isFalse();
        assertThat(jwtService.extractSubject(invalid)).isEmpty();
        assertThat(jwtService.extractRole(invalid)).isEmpty();
        assertThat(jwtService.extractUserId(invalid)).isEmpty();
    }

    @Test
    void shouldFailForInvalidToken() {
        assertThat(jwtService.isValid("invalid.token")).isFalse();
    }

    @Test
    void shouldReturnEmptySubjectForInvalidToken() {
        Optional<String> subject = jwtService.extractSubject("invalid.token");

        assertThat(subject).isEmpty();
    }
}
