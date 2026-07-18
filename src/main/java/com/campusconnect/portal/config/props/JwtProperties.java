package com.campusconnect.portal.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code app.jwt.*}. */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long accessTokenExpirationMs,
        long refreshTokenExpirationMs,
        String issuer
) {
}
