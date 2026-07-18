package com.campusconnect.portal.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code app.email.*}. */
@ConfigurationProperties(prefix = "app.email")
public record EmailProperties(
        String provider,
        String fromEmail,
        String fromName,
        String baseUrl,
        String frontendUrl,
        Brevo brevo
) {
    public record Brevo(String apiKey, String apiUrl) {
    }
}
