package com.campusconnect.portal.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

/**
 * Lightweight authenticated principal placed in the security context by
 * {@link JwtAuthenticationFilter}. Carries just the identity claims needed by controllers
 * and services (via {@code @AuthenticationPrincipal} / {@code CurrentUser}) without a DB load.
 */
public record AuthenticatedUser(
        UUID id,
        String email,
        Collection<? extends GrantedAuthority> authorities
) {
    public boolean hasRole(String role) {
        String withPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return authorities.stream().anyMatch(a -> a.getAuthority().equals(withPrefix));
    }
}
