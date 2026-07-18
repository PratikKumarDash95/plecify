package com.campusconnect.portal.security;

import com.campusconnect.portal.config.props.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Issues and validates stateless access tokens (JWS, HS256). Refresh tokens are opaque and
 * tracked server-side (see {@code RefreshTokenService}); only access tokens are JWTs.
 *
 * <p>The signing key is derived from a Base64-encoded secret; if the configured value is not
 * valid Base64 it is used as raw UTF-8 bytes, so local dev works without extra setup.
 */
@Slf4j
@Service
public class JwtService {

    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_TYPE = "typ";
    private static final String TYPE_ACCESS = "access";

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = buildKey(properties.secret());
    }

    private SecretKey buildKey(String secret) {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT secret must be at least 256 bits (32 bytes). Configure app.jwt.secret.");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UUID userId, String email, List<String> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(properties.accessTokenExpirationMs());
        return Jwts.builder()
                .subject(userId.toString())
                .issuer(properties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_ROLES, roles)
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .signWith(signingKey)
                .compact();
    }

    public Instant getAccessTokenExpiry() {
        return Instant.now().plusMillis(properties.accessTokenExpirationMs());
    }

    public long getAccessTokenTtlSeconds() {
        return properties.accessTokenExpirationMs() / 1000;
    }

    /** Parses and validates a token. Throws {@link JwtException} if invalid/expired. */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(Claims claims) {
        Object roles = claims.get(CLAIM_ROLES);
        return roles instanceof List<?> list ? (List<String>) list : List.of();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Rejected JWT: {}", ex.getMessage());
            return false;
        }
    }
}
