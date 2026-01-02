package com.franco.backend.service.auth;

import com.franco.backend.entity.RefreshToken;
import com.franco.backend.entity.User;
import com.franco.backend.exception.BadRequestException;
import com.franco.backend.repository.RefreshTokenRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final SecureRandom RNG = new SecureRandom();
    private final RefreshTokenRepository repository;

    public RefreshTokenServiceImpl(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public String issue(User user) {
        String token = randomToken();

        RefreshToken rt = new RefreshToken();
        rt.setToken(token);
        rt.setUser(user);
        rt.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS)); // SIMPLE fijo 30d
        rt.setRevoked(false);

        repository.save(rt);
        return token;
    }

    @Override
    @Transactional(readOnly = true)
    public User validateAndGetUser(String refreshToken) {
        RefreshToken rt = repository.findByToken(refreshToken)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        Instant now = Instant.now();

        if (rt.isRevoked() || rt.isExpired(now)) {
            throw new BadRequestException("Refresh token expired or revoked");
        }

        return rt.getUser();
    }

    @Override
    @Transactional
    public void revoke(String refreshToken) {
        repository.findByToken(refreshToken).ifPresent(rt -> {
            rt.setRevoked(true);
        });
    }

    private String randomToken() {
        byte[] buf = new byte[48];
        RNG.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
