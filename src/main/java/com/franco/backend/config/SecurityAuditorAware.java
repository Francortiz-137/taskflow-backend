package com.franco.backend.config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.franco.backend.security.auth.UserPrincipal;

@Component
public class SecurityAuditorAware implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {

        var authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();

        if (authentication == null ||
            !authentication.isAuthenticated() ||
            authentication.getPrincipal() == null) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal.id());
        }

        return Optional.empty();
    }
}
