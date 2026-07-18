package com.campusconnect.portal.service.email;

/**
 * Strategy for physically transmitting a composed {@link EmailMessage} via a concrete
 * provider (Brevo API, SMTP, or a no-op log sink). Selected at runtime by
 * {@code app.email.provider}.
 */
public interface EmailSender {

    /**
     * Transmits the message.
     *
     * @return the provider message id when available, otherwise {@code null}
     * @throws Exception if the provider rejects or fails to accept the message
     */
    String send(EmailMessage message) throws Exception;

    /** @return the {@code app.email.provider} value this sender handles. */
    String provider();
}
