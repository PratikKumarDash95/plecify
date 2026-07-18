package com.campusconnect.portal.service.ai;

import com.campusconnect.portal.dto.ai.AssistAction;
import com.campusconnect.portal.dto.ai.ChatMessageDto;
import com.campusconnect.portal.dto.ai.ChatRequest;
import com.campusconnect.portal.dto.ai.ChatResponse;
import com.campusconnect.portal.dto.ai.TextAssistRequest;
import com.campusconnect.portal.dto.ai.TextAssistResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AiAssistServiceImpl}: prompt construction, message assembly, and the
 * local fallback taken when no live provider is configured.
 */
@ExtendWith(MockitoExtension.class)
class AiAssistServiceImplTest {

    @Mock
    private AiClient aiClient;

    @InjectMocks
    private AiAssistServiceImpl service;

    @Test
    void transform_whenLive_sendsSystemPromptAndText() {
        when(aiClient.isLive()).thenReturn(true);
        when(aiClient.complete(anyList())).thenReturn("polished output");

        TextAssistResponse response = service.transform(
                new TextAssistRequest(AssistAction.POLISH, "raw text", "job description"));

        assertThat(response.result()).isEqualTo("polished output");

        ArgumentCaptor<List<AiClient.Message>> captor = ArgumentCaptor.forClass(List.class);
        verify(aiClient).complete(captor.capture());
        List<AiClient.Message> messages = captor.getValue();
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).role()).isEqualTo("system");
        assertThat(messages.get(0).content()).contains("Polish").contains("job description");
        assertThat(messages.get(1).role()).isEqualTo("user");
        assertThat(messages.get(1).content()).isEqualTo("raw text");
    }

    @Test
    void transform_whenNotLive_usesLocalFallbackWithoutCallingProvider() {
        when(aiClient.isLive()).thenReturn(false);

        TextAssistResponse response = service.transform(
                new TextAssistRequest(AssistAction.SUMMARIZE,
                        "First sentence. Second sentence. Third sentence.", null));

        assertThat(response.result()).isEqualTo("First sentence. Second sentence.");
        verify(aiClient, never()).complete(anyList());
    }

    @Test
    void chat_whenLive_prependsRoleAwareSystemPrompt() {
        when(aiClient.isLive()).thenReturn(true);
        when(aiClient.complete(anyList())).thenReturn("hello student");

        ChatResponse response = service.chat(
                new ChatRequest(List.of(new ChatMessageDto("user", "hi"))), "STUDENT");

        assertThat(response.reply()).isEqualTo("hello student");

        ArgumentCaptor<List<AiClient.Message>> captor = ArgumentCaptor.forClass(List.class);
        verify(aiClient).complete(captor.capture());
        List<AiClient.Message> messages = captor.getValue();
        assertThat(messages.get(0).role()).isEqualTo("system");
        assertThat(messages.get(0).content()).contains("student");
        assertThat(messages.get(1).role()).isEqualTo("user");
        assertThat(messages.get(1).content()).isEqualTo("hi");
    }

    @Test
    void chat_whenNotLive_returnsConfigurationHint() {
        when(aiClient.isLive()).thenReturn(false);

        ChatResponse response = service.chat(
                new ChatRequest(List.of(new ChatMessageDto("user", "hi"))), "COMPANY");

        assertThat(response.reply()).contains("isn't configured");
        verify(aiClient, never()).complete(anyList());
    }

    @Test
    void buildTransformSystemPrompt_coversAllActions() {
        for (AssistAction action : AssistAction.values()) {
            String prompt = service.buildTransformSystemPrompt(action, "text");
            assertThat(prompt).isNotBlank().contains("Return ONLY");
        }
    }
}
