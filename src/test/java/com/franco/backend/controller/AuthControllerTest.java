package com.franco.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.OffsetDateTime;
import java.util.List;

import com.franco.backend.api.GlobalExceptionHandler;
import com.franco.backend.config.CorsProperties;
import com.franco.backend.config.I18nConfig;
import com.franco.backend.config.JacksonConfig;
import com.franco.backend.dto.auth.LoginRequest;
import com.franco.backend.dto.auth.LoginResponse;
import com.franco.backend.dto.auth.RefreshResponse;
import com.franco.backend.dto.user.UserResponse;
import com.franco.backend.entity.UserRole;
import com.franco.backend.exception.AuthenticationException;
import com.franco.backend.security.ratelimit.RateLimitFilter;
import com.franco.backend.service.IAuthService;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;


@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AuthController.class)
@Import({
    GlobalExceptionHandler.class,
    I18nConfig.class,
    JacksonConfig.class
})
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    IAuthService authService;

    @MockitoBean
    CorsProperties corsProperties;

    @MockitoBean
    RateLimitFilter rateLimitFilter;


    private final OffsetDateTime now = OffsetDateTime.now();

    // =========================
    // POST /api/auth/login
    // =========================
    @Nested
    class Login {

        @Test
        void shouldLoginSuccessfully() throws Exception {
            LoginResponse response = new LoginResponse(
                1L,
                "John",
                "user@test.com",
                UserRole.USER,
                now, 
                "refresh-token-123"
            );

            when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "email": "user@test.com",
                          "password": "password"
                        }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-123"));
        }

        @Test
        void shouldFailWhenCredentialsAreInvalid() throws Exception {
            when(authService.login(any()))
                .thenThrow(new AuthenticationException());

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "email": "user@test.com",
                          "password": "wrong-password"
                        }
                    """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        void shouldFailWhenEmailIsInvalid() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "email": "not-an-email",
                          "password": "password"
                        }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        void shouldFailWhenPasswordIsMissing() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "email": "user@test.com"
                        }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        void shouldFailWhenUnknownFieldIsSent() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "email": "user@test.com",
                          "password": "password",
                          "role": "ADMIN"
                        }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }
    }

    // =========================
    // GET /api/auth/me
    // =========================
    @Nested
    class Get
    {
        @Test
        void shouldReturnAuthenticatedUser() throws Exception {

            UserResponse response = new UserResponse(
                1L,
                "John",
                "user@test.com",
                UserRole.USER,
                now,
                now
            );

            when(authService.me(1L)).thenReturn(response);

            mockMvc.perform(get("/api/auth/me")
                    .principal(() -> "user@test.com")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@test.com"));
        }

    }
    
    // =========================
    // POST /api/auth/refresh
    // =========================
    @Nested
    class Refresh {

        @Test
        void shouldReturnAccessToken() throws Exception {

            when(authService.refresh(any()))
                .thenReturn(new RefreshResponse(
                    "new-access-token",
                    "new-refresh-token"
                ));


            mockMvc.perform(post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                        "refreshToken": "refresh-token-123"
                        }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                    .value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken")
                    .value("new-refresh-token"));

        }
    }

    // =========================
    // POST /api/auth/logout
    // =========================
    @Nested
    class Logout {

        @Test
        void shouldLogoutSuccessfully() throws Exception {

            mockMvc.perform(post("/api/auth/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                        "refreshToken": "refresh-token-123"
                        }
                    """))
                .andExpect(status().isNoContent());
        }
    }


}
