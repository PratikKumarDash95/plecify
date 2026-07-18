package com.campusconnect.portal.service.ai;

import com.campusconnect.portal.dto.ai.AssistAction;
import com.campusconnect.portal.dto.ai.ChatMessageDto;
import com.campusconnect.portal.dto.ai.ChatRequest;
import com.campusconnect.portal.dto.ai.ChatResponse;
import com.campusconnect.portal.dto.ai.TextAssistRequest;
import com.campusconnect.portal.dto.ai.TextAssistResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link AiAssistService}. Builds an instruction/system prompt per operation and hands
 * the conversation to the {@link AiClient}. When no live provider is configured it produces a
 * best-effort local result so buttons still do something in dev.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistServiceImpl implements AiAssistService {

    private final AiClient aiClient;

    @Override
    public TextAssistResponse transform(TextAssistRequest request) {
        String context = StringUtils.hasText(request.context()) ? request.context() : "text";
        String system = buildTransformSystemPrompt(request.action(), context);

        if (!aiClient.isLive()) {
            return new TextAssistResponse(localTransform(request.action(), request.text()));
        }

        List<AiClient.Message> messages = List.of(
                AiClient.Message.system(system),
                AiClient.Message.user(request.text()));
        String result = aiClient.complete(messages);
        return new TextAssistResponse(result);
    }

    @Override
    public ChatResponse chat(ChatRequest request, String role) {
        String system = buildChatSystemPrompt(role);

        if (!aiClient.isLive()) {
            return new ChatResponse(localChatReply());
        }

        List<AiClient.Message> messages = new ArrayList<>();
        messages.add(AiClient.Message.system(system));
        for (ChatMessageDto turn : request.messages()) {
            messages.add(new AiClient.Message(turn.role(), turn.content()));
        }
        String reply = aiClient.complete(messages);
        return new ChatResponse(reply);
    }

    /** Instruction prompt for an inline text transformation. Package-private for testability. */
    String buildTransformSystemPrompt(AssistAction action, String context) {
        String instruction = switch (action) {
            case POLISH -> "Polish the following " + context + ". Correct grammar, spelling, and "
                    + "awkward phrasing while preserving the original meaning, tone, and roughly the "
                    + "same length.";
            case SUMMARIZE -> "Summarize the following " + context + " into a concise version that "
                    + "keeps the key points. Be noticeably shorter than the original.";
            case REWRITE_FORMAL -> "Rewrite the following " + context + " in a clear, professional, "
                    + "formal tone suitable for a recruitment platform.";
            case REWRITE_CASUAL -> "Rewrite the following " + context + " in a friendly, "
                    + "conversational tone while staying professional.";
            case EXPAND -> "Expand the following " + context + " into a fuller, well-structured "
                    + "version with more detail, without inventing specific facts.";
        };
        return instruction + " Return ONLY the rewritten text with no preamble, explanation, "
                + "quotation marks, or markdown fences.";
    }

    /** System prompt that grounds the chatbot for the caller's role. Package-private for tests. */
    String buildChatSystemPrompt(String role) {
        String audience = switch (role == null ? "" : role) {
            case "STUDENT" -> "a student using a campus recruitment portal. Help them understand "
                    + "eligibility, applications, deadlines, and improve their cover letters and profiles.";
            case "COMPANY" -> "a company recruiter using a campus recruitment portal. Help them write "
                    + "clear job descriptions, set eligibility criteria, and manage postings.";
            case "PLACEMENT_CELL" -> "a university placement-cell officer. Help them review and "
                    + "approve job postings, communicate with companies, and draft clear notices.";
            case "ADMIN" -> "a platform administrator managing the campus recruitment portal.";
            default -> "a user of a campus recruitment portal.";
        };
        return "You are Plecify Assistant, a helpful assistant embedded in a campus "
                + "recruitment platform. The person you are helping is " + audience
                + " Keep answers concise, practical, and friendly. If asked to write or polish text, "
                + "return clean, ready-to-use prose.";
    }

    // --- local fallback (no live provider) ------------------------------------------------

    private String localTransform(AssistAction action, String text) {
        String cleaned = text.strip().replaceAll("[ \\t]+", " ").replaceAll(" *\\n *", "\n");
        if (action == AssistAction.SUMMARIZE) {
            return firstSentences(cleaned, 2);
        }
        return cleaned;
    }

    /** Returns up to {@code max} leading sentences from the text. */
    private String firstSentences(String text, int max) {
        String[] parts = text.split("(?<=[.!?])\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(max, parts.length); i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    private String localChatReply() {
        return "The AI assistant isn't configured in this environment yet. Once an OpenRouter API "
                + "key and model are set (AI_ENABLED, AI_API_KEY, AI_MODEL), I'll be able to answer "
                + "your questions and help polish your text.";
    }
}
