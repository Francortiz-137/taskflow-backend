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

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                "TEST_SECRET_KEY_123456789012345678901234567890",
                3600
        );
        jwtService = new JwtServiceImpl(properties);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        String token = jwtService.generateToken("user@test.com");

        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    void shouldExtractSubject() {
        String token = jwtService.generateToken("user@test.com");

        Optional<String> subject = jwtService.extractSubject(token);

        assertThat(subject).contains("user@test.com");
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
