package com.campusconnect.portal.service.auth;

import com.campusconnect.portal.config.props.JwtProperties;
import com.campusconnect.portal.entity.RefreshToken;
import com.campusconnect.portal.entity.User;
import com.campusconnect.portal.exception.ErrorCode;
import com.campusconnect.portal.repository.RefreshTokenRepository;
import com.campusconnect.portal.security.SecureTokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Manages the lifecycle of opaque, server-tracked refresh tokens: issuance, rotation, and
 * revocation. Only a SHA-256 hash of each token is persisted; the raw value is returned to
 * the caller exactly once.
 *
 * <p>Rotation is single-use: presenting a valid token issues a fresh pair and revokes the
 * presented one, chaining {@code replacedByTokenHash} for audit and reuse detection.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureTokenGenerator tokenGenerator;
    private final JwtProperties jwtProperties;

    /**
     * Issues a new refresh token for the user and persists its hash.
     *
     * @return the raw token to hand back to the client (never stored)
     */
    @Transactional
    public String issue(User user, String userAgent, String ipAddress) {
        String rawToken = tokenGenerator.generateToken();
        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenGenerator.hash(rawToken))
                .expiresAt(Instant.now().plusMillis(jwtProperties.refreshTokenExpirationMs()))
                .userAgent(truncate(userAgent, 300))
                .ipAddress(truncate(ipAddress, 60))
                .build();
        refreshTokenRepository.save(entity);
        return rawToken;
    }

    /**
     * Validates a presented refresh token and rotates it. The old token is revoked and linked
     * to its replacement; a brand-new raw token is returned.
     *
     * @throws BadCredentialsException if the token is unknown, expired, or already revoked
     */
    @Transactional
    public RotationResult rotate(String rawToken, String userAgent, String ipAddress) {
        RefreshToken current = refreshTokenRepository.findByTokenHash(tokenGenerator.hash(rawToken))
                .orElseThrow(() -> new BadCredentialsException(ErrorCode.TOKEN_INVALID.getDefaultMessage()));

        if (!current.isActive()) {
            // Presenting a revoked token may indicate theft/replay: revoke the whole family.
            log.warn("Refresh token reuse/expiry detected for user {}", current.getUser().getId());
            refreshTokenRepository.revokeAllForUser(current.getUser().getId(), Instant.now());
            throw new BadCredentialsException(ErrorCode.TOKEN_INVALID.getDefaultMessage());
        }

        String newRawToken = tokenGenerator.generateToken();
        String newHash = tokenGenerator.hash(newRawToken);

        current.revoke();
        current.setReplacedByTokenHash(newHash);

        RefreshToken replacement = RefreshToken.builder()
                .user(current.getUser())
                .tokenHash(newHash)
                .expiresAt(Instant.now().plusMillis(jwtProperties.refreshTokenExpirationMs()))
                .userAgent(truncate(userAgent, 300))
                .ipAddress(truncate(ipAddress, 60))
                .build();
        refreshTokenRepository.save(replacement);

        return new RotationResult(current.getUser(), newRawToken);
    }

    /**
     * Deletes the refresh token identified by its raw value (logout). Idempotent: a missing
     * token is a no-op so repeated logouts don't error.
     */
    @Transactional
    public void deleteByRawToken(String rawToken) {
        refreshTokenRepository.findByTokenHash(tokenGenerator.hash(rawToken))
                .ifPresent(refreshTokenRepository::delete);
    }

    /** Revokes every active refresh token for a user (e.g. after a password reset). */
    @Transactional
    public void revokeAll(UUID userId) {
        refreshTokenRepository.revokeAllForUser(userId, Instant.now());
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() > max ? value.substring(0, max) : value;
    }

    /** Outcome of a successful rotation: the owning user and the freshly issued raw token. */
    public record RotationResult(User user, String rawToken) {
    }
}
