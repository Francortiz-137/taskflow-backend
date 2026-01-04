package com.franco.backend.service.auth;

import com.franco.backend.entity.RefreshToken;
import com.franco.backend.entity.User;
import com.franco.backend.exception.BadRequestException;
import com.franco.backend.repository.RefreshTokenRepository;
import com.franco.backend.security.PasswordService;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final SecureRandom RNG = new SecureRandom();
    private final RefreshTokenRepository repository;
    private final PasswordService passwordService;

    public RefreshTokenServiceImpl(RefreshTokenRepository repository, PasswordService passwordService) {
        this.repository = repository;
        this.passwordService = passwordService;
    }

    @Override
    @Transactional
    public String issue(User user) {
        String token = randomToken();
        String hashed = passwordService.hash(token);

        RefreshToken rt = new RefreshToken();
        rt.setTokenHash(hashed);
        rt.setUser(user);
        rt.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS)); // SIMPLE fijo 30d
        rt.setRevoked(false);

        repository.save(rt);
        return token;
    }

    @Override
    @Transactional
    public RotationResult rotate(String refreshToken) {

        List<RefreshToken> candidates = repository.findByRevokedFalse();

        RefreshToken rt = candidates.stream()
            .filter(token ->
                passwordService.matches(
                    refreshToken,
                    token.getTokenHash()
                )
            )
            .findFirst()
            .orElseThrow(() ->
                new BadRequestException("Invalid refresh token")
            );

        Instant now = Instant.now();

        if (rt.isRevoked() || rt.isExpired(now)) {
            throw new BadRequestException("Refresh token expired or revoked");
        }

        rt.setRevoked(true);

        String newRefresh = issue(rt.getUser());

        return new RotationResult(rt.getUser(), newRefresh);
    }


    @Override
    @Transactional
    public void revoke(String refreshToken) {

        List<RefreshToken> candidates = repository.findByRevokedFalse();

        candidates.stream()
            .filter(token ->
                passwordService.matches(
                    refreshToken,
                    token.getTokenHash()
                )
            )
            .findFirst()
            .ifPresent(token -> token.setRevoked(true));
    }


    private String randomToken() {
        byte[] buf = new byte[48];
        RNG.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
