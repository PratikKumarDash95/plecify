package com.campusconnect.portal.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request to transform a block of free text with an inline {@link AssistAction}.
 *
 * @param action  which transformation to apply
 * @param text    the current field text to rewrite
 * @param context optional hint about what the text is (e.g. "job description", "cover letter")
 */
public record TextAssistRequest(
        @NotNull(message = "An action is required")
        AssistAction action,

        @NotBlank(message = "Text is required")
        @Size(max = 20_000, message = "Text must be at most 20000 characters")
        String text,

        @Size(max = 200, message = "Context must be at most 200 characters")
        String context
) {
}
