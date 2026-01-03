package com.franco.backend.service;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.franco.backend.dto.auth.LoginRequest;
import com.franco.backend.dto.auth.LoginResponse;
import com.franco.backend.dto.auth.LogoutRequest;
import com.franco.backend.dto.auth.RefreshRequest;
import com.franco.backend.dto.auth.RefreshResponse;
import com.franco.backend.entity.User;
import com.franco.backend.entity.UserRole;
import com.franco.backend.exception.AuthenticationException;
import com.franco.backend.repository.UserRepository;
import com.franco.backend.security.PasswordService;
import com.franco.backend.security.jwt.JwtService;
import com.franco.backend.service.auth.RefreshTokenService;
import com.franco.backend.service.impl.AuthServiceImpl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordService passwordService;

    @Mock
    RefreshTokenService refreshTokenService;

    @Mock
    JwtService jwtService;


    @InjectMocks
    AuthServiceImpl service;

    @Test
    void shouldLoginSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setName("John");
        user.setPasswordHash("HASH");

        when(userRepository.findByEmail("user@test.com"))
            .thenReturn(Optional.of(user));
        when(passwordService.matches("password", "HASH"))
            .thenReturn(true);
        when(refreshTokenService.issue(any(User.class)))
            .thenReturn("refresh-token-123");


        LoginResponse result = service.login(
            new LoginRequest("user@test.com", "password")
        );

        assertThat(result.email()).isEqualTo("user@test.com");
    }

    @Test
    void shouldFailWhenEmailDoesNotExist() {
        when(userRepository.findByEmail(any()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            service.login(new LoginRequest("x@test.com", "password"))
        ).isInstanceOf(AuthenticationException.class);
    }

    @Test
    void shouldFailWhenPasswordIsInvalid() {
        User user = new User();
        user.setPasswordHash("HASH");

        when(userRepository.findByEmail(any()))
            .thenReturn(Optional.of(user));
        when(passwordService.matches(any(), any()))
            .thenReturn(false);

        assertThatThrownBy(() ->
            service.login(new LoginRequest("x@test.com", "bad"))
        ).isInstanceOf(AuthenticationException.class);
    }

    @Test
    void shouldRefreshAccessToken() {

        User user = new User();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setRole(UserRole.USER);

        when(refreshTokenService.validateAndGetUser("refresh-token-123"))
            .thenReturn(user);

        when(jwtService.generateToken(1L, "user@test.com", UserRole.USER))
            .thenReturn("new-access-token");

        RefreshResponse response = service.refresh(
            new RefreshRequest("refresh-token-123")
        );

        assertThat(response.accessToken())
            .isEqualTo("new-access-token");
    }

    @Test
    void shouldLogout() {

        service.logout(new LogoutRequest("refresh-token-123"));

        verify(refreshTokenService)
            .revoke("refresh-token-123");
    }

}
