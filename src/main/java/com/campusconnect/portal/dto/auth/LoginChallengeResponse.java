package com.campusconnect.portal.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Returned by {@code /auth/login} once credentials are verified: no tokens are issued yet.
 * A one-time code has been emailed; the client must call {@code /auth/verify-otp} with it to
 * complete the login and receive the token pair.
 */
@Schema(description = "Login step-up challenge: an OTP has been emailed and must be verified")
public record LoginChallengeResponse(
        @Schema(description = "Email the OTP was sent to; echo it back to /auth/verify-otp")
        String email,
        @Schema(description = "Always true — signals the client to show the OTP entry step")
        boolean otpRequired,
        @Schema(description = "OTP lifetime in seconds", example = "600") long expiresIn
) {
    public static LoginChallengeResponse of(String email, long expiresIn) {
        return new LoginChallengeResponse(email, true, expiresIn);
    }
}
