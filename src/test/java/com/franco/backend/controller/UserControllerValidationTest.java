package com.franco.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.OffsetDateTime;

import com.franco.backend.api.GlobalExceptionHandler;
import com.franco.backend.config.CorsProperties;
import com.franco.backend.config.I18nConfig;
import com.franco.backend.config.JacksonConfig;
import com.franco.backend.config.JwtProperties;
import com.franco.backend.config.TestSecurityDisableConfig;
import com.franco.backend.dto.user.UserResponse;
import com.franco.backend.entity.UserRole;
import com.franco.backend.exception.EmailAlreadyExistsException;
import com.franco.backend.exception.ResourceNotFoundException;
import com.franco.backend.security.jwt.JwtService;
import com.franco.backend.security.ratelimit.RateLimitFilter;
import com.franco.backend.service.IUserService;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(UserController.class)
@Import({
    GlobalExceptionHandler.class,
    I18nConfig.class,
    JacksonConfig.class,
    TestSecurityDisableConfig.class
})
class UserControllerValidationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    IUserService userService;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    JwtProperties jwtProperties;

    @MockitoBean
    CorsProperties corsProperties;

    @MockitoBean
    RateLimitFilter rateLimitFilter;



    private final OffsetDateTime now = OffsetDateTime.now();

    // =========================
    // POST /api/users
    // =========================
    @Nested
    class CreateUser {

        @Test
        void shouldFailWhenEmailIsInvalid() throws Exception {
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                      {
                        "email": "not-an-email",
                        "password": "password123",
                        "name": "John"
                      }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        void shouldFailWhenUnknownFieldIsSent() throws Exception {
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                      {
                        "email": "user@test.com",
                        "password": "password123",
                        "name": "John",
                        "role": "ADMIN"
                      }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        void shouldFailWhenEmailAlreadyExists() throws Exception {
            when(userService.create(any()))
                .thenThrow(new EmailAlreadyExistsException("user@test.com"));

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                      {
                        "email": "user@test.com",
                        "password": "password123",
                        "name": "John"
                      }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
        }
    }

    // =========================
    // GET /api/users/{id}
    // =========================
    @Nested
    class FindById {

        @Test
        void shouldFailWhenIdIsInvalid() throws Exception {
            mockMvc.perform(get("/api/users/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        void shouldReturn404WhenUserNotFound() throws Exception {
            when(userService.findById(99L))
                .thenThrow(ResourceNotFoundException.userNotFound(99L));

            mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
        }
    }

    // =========================
    // PUT /api/users/{id}/password
    // =========================
    @Nested
    class ChangePassword {

        @Test
        void shouldFailWhenBodyIsInvalid() throws Exception {
            mockMvc.perform(put("/api/users/1/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                      {
                        "currentPassword": "",
                        "newPassword": ""
                      }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        void shouldFailWhenIdIsInvalid() throws Exception {
            mockMvc.perform(put("/api/users/0/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                      {
                        "currentPassword": "oldPass",
                        "newPassword": "newStrongPass123"
                      }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }
    }
}
