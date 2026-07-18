package com.campusconnect.portal.service.ai;

import com.campusconnect.portal.config.props.AiProperties;
import com.campusconnect.portal.exception.ApiException;
import com.campusconnect.portal.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * {@link AiClient} backed by OpenRouter's OpenAI-compatible {@code /chat/completions} API.
 * Mirrors {@code BrevoEmailSender}: builds a {@link RestClient} from the shared builder and
 * authenticates with a bearer token.
 *
 * <p>When AI is disabled or the API key/model is not configured, {@link #isLive()} returns
 * {@code false} and callers should fall back to a local heuristic rather than calling
 * {@link #complete(List)} (which will fail fast with {@link ErrorCode#AI_SERVICE_ERROR}).
 */
@Slf4j
@Component
public class OpenRouterAiClient implements AiClient {

    private final AiProperties properties;
    private final RestClient restClient;

    public OpenRouterAiClient(RestClient.Builder restClientBuilder, AiProperties properties) {
        this.properties = properties;
        int timeout = properties.timeoutMs() > 0 ? (int) properties.timeoutMs() : 30_000;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        this.restClient = restClientBuilder
                .baseUrl(properties.apiUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public boolean isLive() {
        return properties.enabled()
                && StringUtils.hasText(properties.apiKey())
                && StringUtils.hasText(properties.model());
    }

    @Override
    public String complete(List<Message> messages) {
        if (!isLive()) {
            throw new ApiException(ErrorCode.AI_SERVICE_ERROR,
                    "AI provider is not configured. Set AI_ENABLED=true, AI_API_KEY and AI_MODEL.");
        }

        List<Map<String, String>> wireMessages = messages.stream()
                .map(m -> Map.of("role", m.role(), "content", m.content()))
                .toList();
        Map<String, Object> payload = Map.of(
                "model", properties.model(),
                "max_tokens", properties.maxTokens(),
                "messages", wireMessages);

        try {
            ChatCompletionResponse response = restClient.post()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                    // Optional attribution header recommended by OpenRouter.
                    .header("X-Title", "Campus Recruitment Portal")
                    .body(payload)
                    .retrieve()
                    .body(ChatCompletionResponse.class);

            String content = extractContent(response);
            if (!StringUtils.hasText(content)) {
                throw new ApiException(ErrorCode.AI_SERVICE_ERROR, "AI provider returned an empty response.");
            }
            return content.trim();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("OpenRouter completion failed: {}", e.getMessage());
            throw new ApiException(ErrorCode.AI_SERVICE_ERROR, "AI request failed. Please try again.", e);
        }
    }

    private String extractContent(ChatCompletionResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            return null;
        }
        ChatCompletionResponse.Choice first = response.choices().get(0);
        return first.message() != null ? first.message().content() : null;
    }

    /** Minimal projection of the OpenAI-format chat completion response. */
    private record ChatCompletionResponse(List<Choice> choices) {
        private record Choice(Message message) {
        }

        private record Message(String role, String content) {
        }
    }
}
