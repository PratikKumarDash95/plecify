package com.campusconnect.portal.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body variant for email verification (the primary flow is a GET with a {@code token}
 * query parameter; this record supports clients that prefer a POST body).
 */
public record VerifyEmailRequest(
        @NotBlank @Size(max = 512) String token
) {
}
