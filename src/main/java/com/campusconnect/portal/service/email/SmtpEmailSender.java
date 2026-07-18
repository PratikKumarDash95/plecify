package com.campusconnect.portal.service.email;

import com.campusconnect.portal.config.props.EmailProperties;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * SMTP sender (e.g. Brevo SMTP relay) built on Spring's {@link JavaMailSender}. Selected when
 * {@code app.email.provider=smtp}. Requires {@code spring.mail.*} to be configured.
 */
@Slf4j
@Component
public class SmtpEmailSender implements EmailSender {

    public static final String PROVIDER = "smtp";

    private final JavaMailSender mailSender;
    private final EmailProperties properties;

    public SmtpEmailSender(@Autowired(required = false) JavaMailSender mailSender,
                           EmailProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public String send(EmailMessage message) throws Exception {
        if (mailSender == null) {
            throw new IllegalStateException(
                    "SMTP email provider selected but no JavaMailSender is configured (spring.mail.*)");
        }
        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
        helper.setFrom(properties.fromEmail(), properties.fromName());
        helper.setTo(message.toEmail());
        helper.setSubject(message.subject());
        helper.setText(message.textBody(), message.htmlBody());
        mailSender.send(mime);
        return mime.getMessageID();
    }

    @Override
    public String provider() {
        return PROVIDER;
    }
}
