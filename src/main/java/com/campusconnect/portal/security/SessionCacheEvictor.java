package com.campusconnect.portal.security;

import com.campusconnect.portal.config.props.CacheProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Evicts cached {@code UserDetails} from {@link CacheProperties#USER_SESSIONS} when an account's
 * state changes, so a stale principal can never outlive a logout, password reset, or role change.
 *
 * <p>Entries are keyed with {@code id:} / {@code email:} prefixes (see {@code CustomUserDetailsService}).
 * Because either key may be cached independently, callers evict by whichever identifiers they hold.
 */
@Component
@RequiredArgsConstructor
public class SessionCacheEvictor {

    private final CacheManager cacheManager;

    /** Evicts the id-keyed principal. Safe no-op if caching is disabled or the entry is absent. */
    public void evictById(UUID id) {
        if (id != null) {
            userSessions().evict("id:" + id);
        }
    }

    /** Evicts the email-keyed principal (lower-cased to match the cache key). */
    public void evictByEmail(String email) {
        if (email != null) {
            userSessions().evict("email:" + email.toLowerCase());
        }
    }

    /** Evicts both key forms for a user in one call. */
    public void evict(UUID id, String email) {
        evictById(id);
        evictByEmail(email);
    }

    private Cache userSessions() {
        Cache cache = cacheManager.getCache(CacheProperties.USER_SESSIONS);
        if (cache == null) {
            throw new IllegalStateException(
                    "Cache '" + CacheProperties.USER_SESSIONS + "' is not configured");
        }
        return cache;
    }
}
