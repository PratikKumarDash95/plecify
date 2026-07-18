package com.campusconnect.portal.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds {@code app.cache.*}. Backs the in-process Caffeine caches used for session data:
 * resolved user/authority lookups and in-flight two-step login (OTP) challenge state.
 *
 * <p>Caches are per-instance and non-durable by design — they hold transient session data, not
 * a source of truth. TTLs are kept short so account/role changes converge quickly.
 */
@ConfigurationProperties(prefix = "app.cache")
public record CacheProperties(
        boolean enabled,
        Spec userSessions,
        Spec loginSessions
) {

    /** Cache name for resolved {@code UserDetails} keyed by user id / email. */
    public static final String USER_SESSIONS = "user-sessions";

    /** Cache name for in-flight two-step login challenge + attempt state, keyed by email. */
    public static final String LOGIN_SESSIONS = "login-sessions";

    public CacheProperties {
        if (userSessions == null) {
            userSessions = new Spec(10_000, 600);
        }
        if (loginSessions == null) {
            loginSessions = new Spec(10_000, 600);
        }
    }

    /** Per-cache sizing: maximum entries before eviction and time-to-live after write. */
    public record Spec(long maximumSize, long ttlSeconds) {
    }
}
