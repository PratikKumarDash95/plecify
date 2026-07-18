package com.campusconnect.portal.controller;

import com.campusconnect.portal.common.response.ApiResponse;
import com.campusconnect.portal.dto.ai.ChatRequest;
import com.campusconnect.portal.dto.ai.ChatResponse;
import com.campusconnect.portal.dto.ai.TextAssistRequest;
import com.campusconnect.portal.dto.ai.TextAssistResponse;
import com.campusconnect.portal.security.AuthenticatedUser;
import com.campusconnect.portal.security.CurrentUser;
import com.campusconnect.portal.service.ai.AiAssistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI writing assistant available to any authenticated user. Provides inline text transformation
 * (polish/summarize/rewrite) and a role-aware chatbot. Backed by {@link AiAssistService}, which
 * degrades gracefully to a local heuristic when no provider is configured.
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "Inline text polishing and the PlacementPro chatbot")
public class AiController {

    private final AiAssistService aiAssistService;

    @Operation(summary = "Transform text",
            description = "Applies an inline action (POLISH, SUMMARIZE, REWRITE_FORMAL, "
                    + "REWRITE_CASUAL, EXPAND) to a block of text and returns the rewritten result.")
    @PostMapping("/assist")
    public ApiResponse<TextAssistResponse> assist(@Valid @RequestBody TextAssistRequest request) {
        return ApiResponse.success(aiAssistService.transform(request));
    }

    @Operation(summary = "Chat with the assistant",
            description = "Sends the recent conversation and returns the assistant's reply, "
                    + "grounded for the caller's role.")
    @PostMapping("/chat")
    public ApiResponse<ChatResponse> chat(@CurrentUser AuthenticatedUser user,
                                          @Valid @RequestBody ChatRequest request) {
        return ApiResponse.success(aiAssistService.chat(request, primaryRole(user)));
    }

    /** Extracts the role name (without the {@code ROLE_} prefix) from the authenticated user. */
    private String primaryRole(AuthenticatedUser user) {
        if (user == null || user.authorities() == null) {
            return null;
        }
        return user.authorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .findFirst()
                .orElse(null);
    }
}
