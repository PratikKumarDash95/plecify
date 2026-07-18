package com.campusconnect.portal.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Result of a registration flow. No tokens are issued here: the account stays disabled
 * until the email-verification link is followed, so clients receive the created user plus
 * a human-readable next step.
 */
@Schema(description = "Registration result; tokens are issued only after email verification")
public record AuthResponse(
        UserResponse user,
        @Schema(example = "Registration successful. Please check your email to verify your account.")
        String message
) {
}
