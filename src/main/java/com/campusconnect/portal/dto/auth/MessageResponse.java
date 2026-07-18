package com.campusconnect.portal.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/** Minimal payload for endpoints whose result is a human-readable acknowledgement. */
@Schema(description = "Human-readable acknowledgement")
public record MessageResponse(
        @Schema(example = "Your email has been verified. You can now log in.") String message
) {
    public static MessageResponse of(String message) {
        return new MessageResponse(message);
    }
}
