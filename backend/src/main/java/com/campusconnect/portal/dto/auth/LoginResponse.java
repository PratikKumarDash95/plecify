package com.campusconnect.portal.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/** Successful-login payload: the token pair plus the authenticated user's identity. */
@Schema(description = "Access/refresh token pair and the authenticated user")
public record LoginResponse(
        @Schema(description = "Short-lived JWT access token") String accessToken,
        @Schema(description = "Opaque, rotating refresh token") String refreshToken,
        @Schema(description = "Token type for the Authorization header", example = "Bearer")
        String tokenType,
        @Schema(description = "Access-token lifetime in seconds", example = "900") long expiresIn,
        @Schema(description = "Primary role of the user, without the ROLE_ prefix", example = "STUDENT")
        String role,
        UserResponse user
) {
    public static LoginResponse of(String accessToken, String refreshToken, long expiresIn,
                                   String role, UserResponse user) {
        return new LoginResponse(accessToken, refreshToken, "Bearer", expiresIn, role, user);
    }
}
