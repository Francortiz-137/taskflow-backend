package com.franco.backend.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.franco.backend.entity.UserRole;
import com.franco.backend.security.auth.UserPrincipal;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // 1️⃣ Si no hay header o no es Bearer → seguimos sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2️⃣ Extraemos el token
        String token = authHeader.substring(7);

        // 3️⃣ Validamos token
        if (!jwtService.isValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4️⃣ Extraemos datos del token
        String email = jwtService.extractSubject(token).orElse(null);
        UserRole role = jwtService.extractRole(token).orElse(null);
        Long userId = jwtService.extractUserId(token).orElse(null);

        UserPrincipal principal = new UserPrincipal(
                                        userId,
                                        email,
                                        role
                                        );


        if (email != null && role != null && userId != null &&
                 SecurityContextHolder.getContext().getAuthentication() == null) {

            // 5️⃣ Crear Authentication
            UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                        );


            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // 6️⃣ Guardar en contexto
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        }

        // 7️⃣ Continuar request
        filterChain.doFilter(request, response);
    }
}
