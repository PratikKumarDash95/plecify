package com.campusconnect.portal.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** Public projection of an authenticated user. Never exposes the password hash. */
@Schema(description = "Authenticated user profile")
public record UserResponse(
        UUID id,
        String email,
        String fullName,
        String phone,
        boolean emailVerified,
        boolean enabled,
        @Schema(description = "Granted roles, without the ROLE_ prefix", example = "[\"STUDENT\"]")
        Set<String> roles,
        Instant createdAt
) {
}
