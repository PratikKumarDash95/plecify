package com.campusconnect.portal.config;

import com.campusconnect.portal.config.props.CacheProperties;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * Enables annotation-driven caching backed by Caffeine. Each cache is configured independently
 * from {@link CacheProperties} (max size + write TTL) rather than sharing one global spec, so the
 * short-lived login-session cache and the longer user-lookup cache can be tuned separately.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(CacheProperties properties) {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                buildCache(CacheProperties.USER_SESSIONS, properties.userSessions()),
                buildCache(CacheProperties.LOGIN_SESSIONS, properties.loginSessions())));
        return manager;
    }

    private CaffeineCache buildCache(String name, CacheProperties.Spec spec) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .maximumSize(spec.maximumSize())
                .expireAfterWrite(Duration.ofSeconds(spec.ttlSeconds()))
                .build());
    }
}
