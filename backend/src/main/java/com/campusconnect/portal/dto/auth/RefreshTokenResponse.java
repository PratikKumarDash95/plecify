package com.campusconnect.portal.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/** Result of a token refresh: a new access token and the rotated refresh token. */
@Schema(description = "Rotated token pair")
public record RefreshTokenResponse(
        @Schema(description = "New short-lived JWT access token") String accessToken,
        @Schema(description = "New refresh token; the previous one is now invalid") String refreshToken,
        @Schema(example = "Bearer") String tokenType,
        @Schema(example = "900") long expiresIn
) {
    public static RefreshTokenResponse of(String accessToken, String refreshToken, long expiresIn) {
        return new RefreshTokenResponse(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
