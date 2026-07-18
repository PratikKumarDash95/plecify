package com.campusconnect.portal.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds {@code app.ai.*}. Provider-agnostic; defaults to a local heuristic when disabled. */
@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
        boolean enabled,
        String provider,
        String apiKey,
        String apiUrl,
        String model,
        int maxTokens,
        long timeoutMs
) {
}
