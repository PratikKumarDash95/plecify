package com.campusconnect.portal.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/** Binds {@code app.cors.*}. */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        List<String> exposedHeaders,
        boolean allowCredentials,
        long maxAge
) {
}
