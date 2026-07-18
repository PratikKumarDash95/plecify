package com.campusconnect.portal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Extracts and validates the Bearer access token on each request, populating the
 * {@link SecurityContextHolder} from the token's claims. Authorities are rebuilt from the
 * {@code roles} claim so no DB hit is needed on the hot path. Invalid tokens are ignored
 * here; the entry point handles the resulting 401 for protected routes.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtService.parseClaims(token);
                UUID userId = jwtService.extractUserId(claims);
                // The roles claim holds bare role names (e.g. "STUDENT") to match the
                // frontend-visible role. Spring's hasRole(...) expects the ROLE_ prefix, so
                // prepend it here — mirroring UserPrincipal.from() on the login path.
                List<SimpleGrantedAuthority> authorities = jwtService.extractRoles(claims).stream()
                        .map(role -> new SimpleGrantedAuthority(
                                role.startsWith("ROLE_") ? role : "ROLE_" + role))
                        .toList();

                AuthenticatedUser principal = new AuthenticatedUser(
                        userId, claims.get("email", String.class), authorities);

                var authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (StringUtils.hasText(header) && header.startsWith(PREFIX)) {
            return header.substring(PREFIX.length());
        }
        return null;
    }
}
