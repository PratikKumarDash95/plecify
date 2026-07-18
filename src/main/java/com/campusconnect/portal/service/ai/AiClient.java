package com.campusconnect.portal.service.ai;

import java.util.List;

/**
 * Provider-agnostic chat completion abstraction. Implementations translate a system prompt plus
 * an ordered list of chat turns into a single assistant reply.
 */
public interface AiClient {

    /**
     * A single conversational turn. {@code role} is one of {@code system}, {@code user}, or
     * {@code assistant}; {@code content} is the plain-text message.
     */
    record Message(String role, String content) {

        public static Message system(String content) {
            return new Message("system", content);
        }

        public static Message user(String content) {
            return new Message("user", content);
        }

        public static Message assistant(String content) {
            return new Message("assistant", content);
        }
    }

    /**
     * Generates an assistant reply for the given conversation.
     *
     * @param messages ordered turns; the first is typically a {@code system} prompt
     * @return the assistant's plain-text reply (never {@code null})
     */
    String complete(List<Message> messages);

    /** Whether a live provider is configured (enabled + key + model). */
    boolean isLive();
}
