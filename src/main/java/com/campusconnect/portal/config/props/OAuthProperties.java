package com.campusconnect.portal.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code app.oauth.*}. Holds federated-login (Google) client configuration. */
@ConfigurationProperties(prefix = "app.oauth")
public record OAuthProperties(
        Google google
) {
    /**
     * Google OAuth2 client. {@code clientId} is the audience the ID token must be issued for;
     * {@code clientSecret} is accepted for completeness but is not required to verify ID tokens.
     */
    public record Google(
            boolean enabled,
            String clientId,
            String clientSecret
    ) {
    }
}
