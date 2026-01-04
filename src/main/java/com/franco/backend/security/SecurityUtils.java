package com.franco.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.franco.backend.security.auth.UserPrincipal;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof UserPrincipal user) {
            return user.id();
        }

        throw new IllegalStateException("Invalid authentication principal");
    }
}
