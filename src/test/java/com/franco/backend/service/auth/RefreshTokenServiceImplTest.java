package com.franco.backend.service.auth;

import com.franco.backend.entity.RefreshToken;
import com.franco.backend.entity.User;
import com.franco.backend.exception.BadRequestException;
import com.franco.backend.repository.RefreshTokenRepository;
import com.franco.backend.security.PasswordService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    RefreshTokenRepository repository;

    @Mock
    PasswordService passwordService;

    @InjectMocks
    RefreshTokenServiceImpl service;

    // =========================
    // issue()
    // =========================
    @Test
    void shouldIssueHashedRefreshToken() {

        User user = new User();

        when(passwordService.hash(any()))
            .thenReturn("HASHED_TOKEN");

        ArgumentCaptor<RefreshToken> captor =
            ArgumentCaptor.forClass(RefreshToken.class);

        String token = service.issue(user);

        verify(repository).save(captor.capture());

        RefreshToken saved = captor.getValue();

        assertThat(token).isNotNull();
        assertThat(saved.getTokenHash()).isEqualTo("HASHED_TOKEN");
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.isRevoked()).isFalse();
        assertThat(saved.getExpiresAt())
            .isAfter(Instant.now());
    }

    // =========================
    // rotate()
    // =========================
    @Test
    void shouldRotateRefreshToken() {

        User user = new User();

        RefreshToken existing = new RefreshToken();
        existing.setUser(user);
        existing.setTokenHash("OLD_HASH");
        existing.setRevoked(false);
        existing.setExpiresAt(Instant.now().plusSeconds(60));

        when(repository.findByRevokedFalse())
            .thenReturn(List.of(existing));

        when(passwordService.matches("old-token", "OLD_HASH"))
            .thenReturn(true);

        when(passwordService.hash(any()))
            .thenReturn("NEW_HASH");

        RefreshTokenService.RotationResult result =
            service.rotate("old-token");

        assertThat(existing.isRevoked()).isTrue();
        assertThat(result.user()).isEqualTo(user);
        assertThat(result.refreshToken()).isNotNull();

        verify(repository).save(any());
    }

    @Test
    void shouldFailRotateWhenTokenIsInvalid() {

        when(repository.findByRevokedFalse())
            .thenReturn(List.of());

        assertThatThrownBy(() ->
            service.rotate("invalid-token")
        ).isInstanceOf(BadRequestException.class);
    }

    // =========================
    // revoke()
    // =========================
    @Test
    void shouldRevokeToken() {

        RefreshToken token = new RefreshToken();
        token.setTokenHash("HASH");
        token.setRevoked(false);

        when(repository.findByRevokedFalse())
            .thenReturn(List.of(token));

        when(passwordService.matches("refresh-token", "HASH"))
            .thenReturn(true);

        service.revoke("refresh-token");

        assertThat(token.isRevoked()).isTrue();
    }

    @Test
    void shouldRevokeAllTokensOnReuseAttack() {

        User user = new User();

        RefreshToken revoked = new RefreshToken();
        revoked.setUser(user);
        revoked.setTokenHash("HASH");
        revoked.setRevoked(true);
        revoked.setExpiresAt(Instant.now().plusSeconds(60));

        RefreshToken other = new RefreshToken();
        other.setUser(user);
        other.setTokenHash("OTHER_HASH");
        other.setRevoked(false);
        other.setExpiresAt(Instant.now().plusSeconds(60));


        when(repository.findByRevokedFalse())
            .thenReturn(List.of(other));

        when(repository.findByRevokedTrue())
            .thenReturn(List.of(revoked));

        when(repository.findByUser(user))
            .thenReturn(List.of(revoked, other));

        when(passwordService.matches("reuse-token", "HASH"))
            .thenReturn(true);
        when(passwordService.matches("reuse-token", "OTHER_HASH"))
            .thenReturn(false);


        assertThatThrownBy(() ->
            service.rotate("reuse-token")
        ).isInstanceOf(BadRequestException.class);

        assertThat(revoked.isRevoked()).isTrue();
        assertThat(other.isRevoked()).isTrue();
    }

}
