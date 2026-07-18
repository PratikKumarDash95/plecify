package com.campusconnect.portal.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Payload for Sign in with Google: the ID token (JWT) obtained by the frontend from Google. */
@Schema(description = "Google ID token issued to the frontend by Google Identity Services")
public record GoogleLoginRequest(
        @Schema(description = "Google-issued ID token (JWT credential)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "idToken is required")
        String idToken
) {
}
