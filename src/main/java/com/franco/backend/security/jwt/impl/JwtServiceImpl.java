package com.franco.backend.security.jwt.impl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.franco.backend.config.JwtProperties;
import com.franco.backend.entity.UserRole;
import com.franco.backend.security.jwt.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;

@Service
public class JwtServiceImpl implements JwtService {

    private static final String ROLE_CLAIM = "role";
    private static final String USER_ID_CLAIM = "userId";


    private final SecretKey secretKey;
    private final JwtProperties properties;

    public JwtServiceImpl(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(
            properties.secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public String generateToken(Long userId, String subject, UserRole role) {

        Instant now = Instant.now();
        Instant expiration = now.plus(
            properties.expirationMinutes(),
            ChronoUnit.MINUTES
        );

        return Jwts.builder()
            .subject(subject)
            .claim(USER_ID_CLAIM, userId)
            .claim(ROLE_CLAIM, role.name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey)
            .compact();
    }


    @Override
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public Optional<String> extractSubject(String token) {
        try {
            return Optional.ofNullable(parseClaims(token).getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserRole> extractRole(String token) {
        try {
            Claims claims = parseClaims(token);
            String role = claims.get(ROLE_CLAIM, String.class);
            return Optional.ofNullable(role).map(UserRole::valueOf);
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Long> extractUserId(String token) {
        try {
            Object value = parseClaims(token).get(USER_ID_CLAIM);

            if (value instanceof Integer i) {
                return Optional.of(i.longValue());
            }
            if (value instanceof Long l) {
                return Optional.of(l);
            }
            if (value instanceof String s) {
                return Optional.of(Long.parseLong(s));
            }
            return Optional.empty();

        } catch (Exception ex) {
            return Optional.empty();
        }
    }


    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
