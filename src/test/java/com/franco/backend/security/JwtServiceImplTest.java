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
    void shouldGenerateAndValidateToken() {
        String token = jwtService.generateToken(
            "user@test.com",
            UserRole.USER
        );

        assertThat(token).isNotBlank();
        assertThat(jwtService.isValid(token)).isTrue();
    }


    @Test
    void shouldExtractSubjectAndRole() {
        String token = jwtService.generateToken(
            "user@test.com",
            UserRole.ADMIN
        );

        Optional<String> subject = jwtService.extractSubject(token);
        Optional<UserRole> role = jwtService.extractRole(token);

        assertThat(subject).contains("user@test.com");
        assertThat(role).contains(UserRole.ADMIN);
    }

    @Test
    void shouldReturnEmptyWhenTokenIsExpired() {
        JwtProperties shortLivedProps = new JwtProperties(
            properties.secret(),
            1/60 // 1 second
        );

        JwtService shortLivedService = new JwtServiceImpl(shortLivedProps);

        String token = shortLivedService.generateToken(
            "user@test.com",
            UserRole.USER
        );

        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}

        assertThat(shortLivedService.isValid(token)).isFalse();
        assertThat(shortLivedService.extractSubject(token)).isEmpty();
        assertThat(shortLivedService.extractRole(token)).isEmpty();
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
