package com.franco.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import java.util.List;

import com.franco.backend.api.GlobalExceptionHandler;
import com.franco.backend.config.*;
import com.franco.backend.security.auth.UserPrincipal;
import com.franco.backend.entity.UserRole;
import com.franco.backend.service.IUserService;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import({
    MethodSecurityConfig.class,
    GlobalExceptionHandler.class,
    I18nConfig.class,
    JacksonConfig.class
})
class UserControllerAuthorizationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    IUserService userService;

    @MockitoBean
    CorsProperties corsProperties;

    // =========================
    // GET /api/users
    // =========================

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToGetUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldForbidNonAdminToGetUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isForbidden());
    }

    // =========================
    // PUT /api/users/{id}/password
    // =========================
    @Nested
    class ChangePassword {

        @Test
        void shouldAllowOwnerToChangePassword() throws Exception {
            UserPrincipal principal =
                new UserPrincipal(1L, "user@test.com", UserRole.USER);

            doNothing().when(userService).changePassword(eq(1L), any());

            mockMvc.perform(put("/api/users/1/password")
                    .with(authentication(
                        new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        )
                    ))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                      {
                        "currentPassword": "oldPass",
                        "newPassword": "newStrongPass123"
                      }
                    """))
                .andExpect(status().isNoContent());
        }

        @Test
        void shouldForbidChangingOtherUserPassword() throws Exception {
            UserPrincipal principal =
                new UserPrincipal(2L, "other@test.com", UserRole.USER);

            mockMvc.perform(put("/api/users/1/password")
                    .with(authentication(
                        new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        )
                    ))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                      {
                        "currentPassword": "oldPass",
                        "newPassword": "newStrongPass123"
                      }
                    """))
                .andExpect(status().isForbidden());
        }
    }
}
