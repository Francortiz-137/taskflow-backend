package com.franco.backend.security.jwt.impl;

import com.franco.backend.config.JwtProperties;
import com.franco.backend.security.jwt.JwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtProperties properties;

    @Override
    public String generateToken(String subject) {

        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(properties.expirationSeconds());

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(
                        Keys.hmacShaKeyFor(
                                properties.secret().getBytes(StandardCharsets.UTF_8)
                        ),
                        SignatureAlgorithm.HS256
                )
                .compact();
    }

    @Override
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public Optional<String> extractSubject(String token) {
        try {
            Claims claims = parse(token).getBody();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(
                        Keys.hmacShaKeyFor(
                                properties.secret().getBytes(StandardCharsets.UTF_8)
                        )
                )
                .build()
                .parseClaimsJws(token);
    }
}
