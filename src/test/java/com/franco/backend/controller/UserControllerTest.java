package com.franco.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.OffsetDateTime;
import java.util.List;

import com.franco.backend.api.GlobalExceptionHandler;
import com.franco.backend.config.CorsProperties;
import com.franco.backend.config.I18nConfig;
import com.franco.backend.config.JacksonConfig;
import com.franco.backend.dto.CreateUserRequest;
import com.franco.backend.dto.UserResponse;
import com.franco.backend.entity.UserRole;
import com.franco.backend.exception.EmailAlreadyExistsException;
import com.franco.backend.exception.ResourceNotFoundException;
import com.franco.backend.service.IUserService;
import com.franco.backend.config.SecurityConfig;
import com.franco.backend.exception.BadRequestException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(UserController.class)
@Import({
    GlobalExceptionHandler.class,
    I18nConfig.class,
    JacksonConfig.class
})
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private IUserService userService;

    @MockitoBean
    CorsProperties corsProperties;


    private final OffsetDateTime now = OffsetDateTime.now();

    // =========================
    // POST /api/users
    // =========================
    @Nested
    class CreateUser {

        @Test
        void shouldCreateUser() throws Exception {
            UserResponse response = new UserResponse(
                1L,
                "John",
                "user@test.com",
                UserRole.USER,
                now.minusDays(1),
                now
            );

            when(userService.create(any()))
                .thenReturn(response);

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Accept-Language", "en")
                    .content("""
                        {
                          "email": "user@test.com",
                          "password": "password",
                          "name": "John"
                        }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
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
                          "password": "password",
                          "name": "John"
                        }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        void shouldFailWhenEmailIsInvalid() throws Exception {
            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "email": "not-an-email",
                          "password": "password",
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
                        "name": "John",
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
    // GET /api/users/{id}
    // =========================
    @Nested
    class FindById {

        @Test
        void shouldReturnUserById() throws Exception {
            UserResponse response = new UserResponse(
                1L,
                "John",
                "user@test.com",
                UserRole.USER,
                now,
                now
            );

            when(userService.findById(1L)).thenReturn(response);

            mockMvc.perform(get("/api/users/1")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        void shouldReturn404WhenUserNotFound() throws Exception {
            when(userService.findById(99L))
                .thenThrow(ResourceNotFoundException.userNotFound(99L));

            mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        void shouldFailWhenIdIsInvalid() throws Exception {
            mockMvc.perform(get("/api/users/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }
    }

    // =========================
    // GET /api/users
    // =========================
    @Nested
    class FindAll {

        @Test
        void shouldReturnAllUsers() throws Exception {
            List<UserResponse> users = List.of(
                new UserResponse(1L, "a@test.com", "A", UserRole.USER, now, now),
                new UserResponse(2L, "b@test.com", "B", UserRole.USER, now, now)
            );

            when(userService.findAll()).thenReturn(users);

            mockMvc.perform(get("/api/users")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void shouldReturnEmptyList() throws Exception {
            when(userService.findAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // =========================
// PUT /api/users/{id}
// =========================
    @Nested
    class UpdateUser {

        @Test
        void shouldUpdateUserName() throws Exception {
            UserResponse response = new UserResponse(
                1L,
                "New Name",
                "user@test.com",
                UserRole.USER,
                now,
                now
            );

            when(userService.update(eq(1L), any()))
                .thenReturn(response);

            mockMvc.perform(put("/api/users/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        { "name": "New Name" }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
        }

        @Test
        void shouldFailWhenUserNotFound() throws Exception {
            when(userService.update(eq(99L), any()))
                .thenThrow(ResourceNotFoundException.userNotFound(99L));

            mockMvc.perform(put("/api/users/99")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                        "name": "New Name"
                        }
                    """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
        }

        @Test
        void shouldFailWhenIdIsInvalid() throws Exception {
            mockMvc.perform(put("/api/users/0")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                        "name": "New Name"
                        }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }

        @Test
        void shouldFailWhenBodyIsInvalid() throws Exception {
            mockMvc.perform(put("/api/users/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        { "name": "" }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }


    }


}
