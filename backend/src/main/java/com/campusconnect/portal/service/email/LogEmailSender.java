package com.campusconnect.portal.service.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Development/test sender that logs the email instead of transmitting it. Selected when
 * {@code app.email.provider=log} (the default in the dev profile), so local signups work
 * without SMTP or Brevo credentials.
 */
@Slf4j
@Component
public class LogEmailSender implements EmailSender {

    public static final String PROVIDER = "log";

    @Override
    public String send(EmailMessage message) {
        String messageId = "log-" + UUID.randomUUID();
        log.info("""
                [LOG EMAIL] would send email
                  to       : {} <{}>
                  subject  : {}
                  template : {}
                  body     :
                {}""",
                message.toName(), message.toEmail(), message.subject(),
                message.template(), message.textBody());
        return messageId;
    }

    @Override
    public String provider() {
        return PROVIDER;
    }
}
