package com.campusconnect.portal.security;

import com.campusconnect.portal.config.props.CacheProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * In-process session store for the two-step (OTP) login handshake, backed by
 * {@link CacheProperties#LOGIN_SESSIONS}. It holds transient challenge state between {@code login}
 * and {@code verify-otp} — most importantly a failed-attempt counter that throttles brute-force
 * guessing of the low-entropy 6-digit code.
 *
 * <p>The emailed code itself remains persisted (hashed) by {@code VerificationTokenService}; this
 * cache is a throttle/session layer, not the source of truth. Entries are keyed by normalised email
 * and expire with the cache TTL (aligned to the OTP lifetime), so an abandoned challenge cleans
 * itself up.
 */
@Component
@RequiredArgsConstructor
public class LoginSessionCache {

    /** Failed OTP attempts allowed before the challenge is locked and must be re-requested. */
    public static final int MAX_ATTEMPTS = 5;

    private final CacheManager cacheManager;

    /**
     * Transient login-challenge state. {@code attempts} counts failed OTP submissions; the code is
     * never stored here (it lives hashed in the DB).
     */
    public record Challenge(UUID userId, int attempts, Instant issuedAt) {

        Challenge withAttempt() {
            return new Challenge(userId, attempts + 1, issuedAt);
        }
    }

    /** Records that an OTP challenge was issued for {@code email}, resetting any prior attempts. */
    public void startChallenge(String email, UUID userId) {
        cache().put(key(email), new Challenge(userId, 0, Instant.now()));
    }

    /** Returns the live challenge for {@code email}, or {@code null} if none is cached / it expired. */
    public Challenge get(String email) {
        return cache().get(key(email), Challenge.class);
    }

    /**
     * Records a failed OTP attempt and reports whether the caller may keep trying.
     *
     * @return {@code true} if further attempts remain; {@code false} once {@link #MAX_ATTEMPTS}
     *         is reached (the challenge is evicted and must be re-requested)
     */
    public boolean registerFailedAttempt(String email) {
        Challenge current = get(email);
        if (current == null) {
            return false;
        }
        Challenge next = current.withAttempt();
        if (next.attempts() >= MAX_ATTEMPTS) {
            clear(email);
            return false;
        }
        cache().put(key(email), next);
        return true;
    }

    /** Clears the challenge once login completes (or the attempt budget is exhausted). */
    public void clear(String email) {
        cache().evict(key(email));
    }

    private String key(String email) {
        return email == null ? "" : email.toLowerCase();
    }

    private Cache cache() {
        Cache cache = cacheManager.getCache(CacheProperties.LOGIN_SESSIONS);
        if (cache == null) {
            throw new IllegalStateException(
                    "Cache '" + CacheProperties.LOGIN_SESSIONS + "' is not configured");
        }
        return cache;
    }
}
