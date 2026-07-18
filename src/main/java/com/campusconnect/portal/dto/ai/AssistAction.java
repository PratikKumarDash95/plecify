package com.campusconnect.portal.dto.ai;

/**
 * Inline text-assist operations offered on free-text fields (job descriptions, cover letters,
 * rejection reasons). Each maps to a distinct instruction handed to the AI provider.
 */
public enum AssistAction {
    /** Fix grammar, spelling, and phrasing without changing meaning or length much. */
    POLISH,
    /** Condense into a shorter, tighter version that keeps the key points. */
    SUMMARIZE,
    /** Rewrite in a professional, formal tone. */
    REWRITE_FORMAL,
    /** Rewrite in a relaxed, conversational tone. */
    REWRITE_CASUAL,
    /** Expand a short draft into a fuller, well-structured piece. */
    EXPAND
}
