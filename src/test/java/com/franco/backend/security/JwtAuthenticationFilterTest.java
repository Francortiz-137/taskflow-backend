package com.franco.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.franco.backend.entity.UserRole;
import com.franco.backend.security.jwt.JwtAuthenticationFilter;
import com.franco.backend.security.jwt.JwtService;
import com.franco.backend.security.auth.UserPrincipal;


@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    JwtService jwtService;

    @Mock
    FilterChain filterChain;

    @InjectMocks
    JwtAuthenticationFilter filter;

    MockHttpServletRequest request;
    MockHttpServletResponse response;

    @BeforeEach
    void setup() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateRequestWhenJwtIsValid() throws Exception {

        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtService.isValid("valid-token")).thenReturn(true);
        when(jwtService.extractSubject("valid-token"))
            .thenReturn(Optional.of("user@test.com"));
        when(jwtService.extractRole("valid-token"))
            .thenReturn(Optional.of(UserRole.USER));
        when(jwtService.extractUserId("valid-token"))
            .thenReturn(Optional.of(1L));

        filter.doFilter(request, response, filterChain);

        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();

        assertThat(auth).isNotNull();

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

        assertThat(principal.id()).isEqualTo(1L);
        assertThat(principal.email()).isEqualTo("user@test.com");
        assertThat(principal.role()).isEqualTo(UserRole.USER);

        assertThat(auth.getAuthorities())
            .extracting("authority")
            .containsExactly("ROLE_USER");


        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenNoToken() throws Exception {

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
            .isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenTokenIsInvalid() throws Exception {

        request.addHeader("Authorization", "Bearer bad-token");

        when(jwtService.isValid("bad-token")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
            .isNull();

        verify(filterChain).doFilter(request, response);
    }
}
