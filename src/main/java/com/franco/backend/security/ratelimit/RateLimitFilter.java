package com.franco.backend.security.ratelimit;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.franco.backend.config.RateLimitProperties;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties properties;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String ip = request.getRemoteAddr();

        if (path.equals("/api/auth/login")) {
            if (!rateLimitService.allow("login:" + ip, properties.loginPerMinute())) {
                response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many login attempts");
                return;
            }
        }

        if (path.equals("/api/auth/refresh")) {
            if (!rateLimitService.allow("refresh:" + ip, properties.refreshPerMinute())) {
                response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Too many refresh attempts");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
