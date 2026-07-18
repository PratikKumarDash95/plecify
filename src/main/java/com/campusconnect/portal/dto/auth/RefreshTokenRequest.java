package com.campusconnect.portal.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Carries the opaque refresh token to be rotated for a new access/refresh pair. */
public record RefreshTokenRequest(
        @NotBlank @Size(max = 512) String refreshToken
) {
}
