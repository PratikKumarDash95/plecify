package com.campusconnect.portal.service.ai;

import com.campusconnect.portal.dto.ai.ChatRequest;
import com.campusconnect.portal.dto.ai.ChatResponse;
import com.campusconnect.portal.dto.ai.TextAssistRequest;
import com.campusconnect.portal.dto.ai.TextAssistResponse;

/**
 * High-level AI helper for the portal: inline text rewriting and a role-aware chatbot.
 * Delegates generation to an {@link AiClient}; falls back to a local heuristic when no live
 * provider is configured so the UI stays functional in development.
 */
public interface AiAssistService {

    /**
     * Applies an inline transformation (polish, summarize, rewrite) to a block of text.
     *
     * @param request the action, source text, and optional context
     * @return the rewritten text
     */
    TextAssistResponse transform(TextAssistRequest request);

    /**
     * Answers a chatbot conversation, grounded for the given user role.
     *
     * @param request the recent conversation turns
     * @param role    the caller's role name (e.g. {@code STUDENT}); may be {@code null}
     * @return the assistant reply
     */
    ChatResponse chat(ChatRequest request, String role);
}
