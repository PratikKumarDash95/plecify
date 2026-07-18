package com.campusconnect.portal.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/** Binds {@code app.storage.*}. */
@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(
        String provider,
        Supabase supabase,
        long maxResumeSizeBytes,
        List<String> allowedResumeTypes,
        List<String> allowedImageTypes
) {
    public record Supabase(
            String url,
            String serviceKey,
            String bucketResumes,
            String bucketLogos,
            String bucketAvatars
    ) {
    }
}
