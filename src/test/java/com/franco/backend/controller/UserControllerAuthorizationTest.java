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
import com.franco.backend.security.ratelimit.RateLimitFilter;
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

    @MockitoBean
    RateLimitFilter rateLimitFilter;

    @Test
    void shouldAllowAdminToGetUsers() throws Exception {
        UserPrincipal admin =
            new UserPrincipal(1L, "admin@test.com", UserRole.ADMIN);

        mockMvc.perform(get("/api/users")
                .with(authentication(
                    new UsernamePasswordAuthenticationToken(
                        admin,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                    )
                )))
            .andExpect(status().isOk());
    }
}
