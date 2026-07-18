package com.campusconnect.portal.service.email;

import com.campusconnect.portal.config.props.EmailProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Sends transactional email through the Brevo (formerly Sendinblue) HTTP API. Selected when
 * {@code app.email.provider=brevo}. Authenticates with the {@code api-key} header and posts
 * to {@code /v3/smtp/email}.
 */
@Slf4j
@Component
public class BrevoEmailSender implements EmailSender {

    public static final String PROVIDER = "brevo";

    private final RestClient restClient;
    private final EmailProperties properties;

    public BrevoEmailSender(RestClient.Builder restClientBuilder, EmailProperties properties) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(properties.brevo().apiUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String send(EmailMessage message) {
        if (!StringUtils.hasText(properties.brevo().apiKey())) {
            throw new IllegalStateException(
                    "Brevo email provider selected but app.email.brevo.api-key is not configured");
        }
        Map<String, Object> payload = Map.of(
                "sender", Map.of("email", properties.fromEmail(), "name", properties.fromName()),
                "to", List.of(Map.of("email", message.toEmail(), "name",
                        StringUtils.hasText(message.toName()) ? message.toName() : message.toEmail())),
                "subject", message.subject(),
                "htmlContent", message.htmlBody(),
                "textContent", message.textBody());

        BrevoResponse response = restClient.post()
                .header("api-key", properties.brevo().apiKey())
                .body(payload)
                .retrieve()
                .body(BrevoResponse.class);

        return response != null ? response.messageId() : null;
    }

    @Override
    public String provider() {
        return PROVIDER;
    }

    /** Minimal projection of the Brevo send response. */
    private record BrevoResponse(String messageId) {
    }
}
