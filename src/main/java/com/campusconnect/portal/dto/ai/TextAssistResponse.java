package com.campusconnect.portal.dto.ai;

/**
 * Result of a {@link TextAssistRequest}.
 *
 * @param result the rewritten text, ready to drop back into the field
 */
public record TextAssistResponse(String result) {
}
