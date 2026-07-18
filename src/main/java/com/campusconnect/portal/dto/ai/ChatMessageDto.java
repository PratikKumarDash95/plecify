package com.campusconnect.portal.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * A single turn in a chatbot conversation.
 *
 * @param role    either {@code user} or {@code assistant}
 * @param content the message text
 */
public record ChatMessageDto(
        @NotBlank(message = "Role is required")
        @Pattern(regexp = "user|assistant", message = "Role must be 'user' or 'assistant'")
        String role,

        @NotBlank(message = "Message content is required")
        @Size(max = 8_000, message = "Message must be at most 8000 characters")
        String content
) {
}
