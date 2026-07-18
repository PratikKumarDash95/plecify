package com.campusconnect.portal.config;

import com.campusconnect.portal.security.AuthenticatedUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Supplies the current principal's email for JPA auditing ({@code createdBy}/{@code updatedBy}).
 * Falls back to {@code system} for unauthenticated flows (bootstrap, scheduled jobs).
 */
@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<String> {

    private static final String SYSTEM = "system";

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.of(SYSTEM);
        }
        if (auth.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.of(user.email());
        }
        return Optional.of(auth.getName() != null ? auth.getName() : SYSTEM);
    }
}
