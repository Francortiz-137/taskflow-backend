package com.franco.backend.testutil;

import com.franco.backend.security.auth.UserPrincipal;
import com.franco.backend.entity.UserRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityTestUtils {

    public static void authenticate(Long userId) {
        var principal = new UserPrincipal(userId, "test@test.com", UserRole.USER);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}
