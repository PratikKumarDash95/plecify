package com.campusconnect.portal.dto.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * A chatbot request carrying the recent conversation. The last message is expected to be the
 * user's newest turn; earlier turns provide context.
 *
 * @param messages ordered conversation turns (oldest first)
 */
public record ChatRequest(
        @NotEmpty(message = "At least one message is required")
        @Size(max = 20, message = "Conversation is limited to 20 messages")
        @Valid
        List<ChatMessageDto> messages
) {
}
