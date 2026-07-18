package com.campusconnect.portal.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code app.security.*}. */
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        long verificationTokenExpirationMs,
        long resetTokenExpirationMs,
        long otpExpirationMs,
        RateLimit rateLimit
) {
    public record RateLimit(
            boolean enabled,
            long capacity,
            long refillTokens,
            long refillDurationSeconds,
            long authCapacity
    ) {
    }
}
