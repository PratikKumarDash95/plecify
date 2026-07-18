package com.campusconnect.portal.service.email;

import com.campusconnect.portal.config.props.EmailProperties;
import com.campusconnect.portal.entity.EmailLog;
import com.campusconnect.portal.repository.EmailLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Default {@link EmailService}. Composes branded HTML/text bodies, selects the configured
 * {@link EmailSender} provider, and records every attempt in {@code email_logs}. Delivery is
 * asynchronous ({@code @Async}) and self-contained: failures are logged and persisted but
 * never propagated, so a mail outage cannot roll back the registration or reset that
 * triggered it.
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final EmailProperties properties;
    private final EmailLogRepository emailLogRepository;
    private final EmailSender sender;

    public EmailServiceImpl(EmailProperties properties,
                            EmailLogRepository emailLogRepository,
                            List<EmailSender> senders) {
        this.properties = properties;
        this.emailLogRepository = emailLogRepository;
        this.sender = resolveSender(senders, properties.provider());
    }

    private EmailSender resolveSender(List<EmailSender> senders, String configured) {
        return senders.stream()
                .filter(s -> s.provider().equalsIgnoreCase(configured))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No EmailSender registered for provider '" + configured + "'. Available: "
                                + senders.stream().map(EmailSender::provider).toList()));
    }

    @Override
    @Async
    public void sendEmailVerification(String toEmail, String recipientName, String verifyUrl) {
        String name = safeName(recipientName);
        String subject = "Verify your CampusConnect account";
        String text = """
                Hi %s,

                Welcome to CampusConnect. Please verify your email address to activate your account:

                %s

                This link expires in 24 hours. If you did not create an account, you can ignore this email.

                — %s""".formatted(name, verifyUrl, properties.fromName());
        String html = htmlTemplate("Verify your email",
                "Welcome to CampusConnect, " + escape(name) + "!",
                "Please verify your email address to activate your account.",
                "Verify email", verifyUrl,
                "This link expires in 24 hours. If you did not create an account, you can ignore this email.");
        dispatch(new EmailMessage(toEmail, name, subject, html, text, "email-verification"));
    }

    @Override
    @Async
    public void sendPasswordReset(String toEmail, String recipientName, String resetUrl) {
        String name = safeName(recipientName);
        String subject = "Reset your CampusConnect password";
        String text = """
                Hi %s,

                We received a request to reset your password. Use the link below to choose a new one:

                %s

                This link expires in 1 hour. If you did not request a reset, you can safely ignore this email.

                — %s""".formatted(name, resetUrl, properties.fromName());
        String html = htmlTemplate("Reset your password",
                "Password reset requested",
                "We received a request to reset the password for " + escape(name) + ".",
                "Reset password", resetUrl,
                "This link expires in 1 hour. If you did not request a reset, you can safely ignore this email.");
        dispatch(new EmailMessage(toEmail, name, subject, html, text, "password-reset"));
    }

    @Override
    @Async
    public void sendLoginOtp(String toEmail, String recipientName, String code, long expiryMinutes) {
        String name = safeName(recipientName);
        String subject = "Your CampusConnect login code";
        String text = """
                Hi %s,

                Use this one-time code to finish signing in:

                %s

                This code expires in %d minutes. If you did not try to sign in, someone may have your
                password — reset it immediately.

                — %s""".formatted(name, code, expiryMinutes, properties.fromName());
        String html = otpTemplate(
                "Your login code",
                "Verify it's you, " + escape(name),
                "Enter this one-time code to finish signing in to CampusConnect:",
                code,
                "This code expires in " + expiryMinutes + " minutes. If you did not try to sign in, "
                        + "someone may have your password — reset it immediately.");
        dispatch(new EmailMessage(toEmail, name, subject, html, text, "login-otp"));
    }

    @Override
    @Async
    public void sendJobApproved(String toEmail, String recipientName, String jobTitle,
                                int eligibleCount, String jobUrl) {
        String name = safeName(recipientName);
        String subject = "Your job posting is live: " + jobTitle;
        String text = """
                Hi %s,

                Good news — your job posting "%s" has been approved and is now visible to eligible students.

                It matched %d eligible student(s), who have been notified.

                View the posting: %s

                — %s""".formatted(name, jobTitle, eligibleCount, jobUrl, properties.fromName());
        String html = htmlTemplate("Your job posting is live",
                "Job approved",
                "Your posting \"" + escape(jobTitle) + "\" is now visible to eligible students. It matched "
                        + eligibleCount + " eligible student(s).",
                "View posting", jobUrl,
                "You are receiving this because you posted a job on CampusConnect.");
        dispatch(new EmailMessage(toEmail, name, subject, html, text, "job-approved"));
    }

    @Override
    @Async
    public void sendJobRejected(String toEmail, String recipientName, String jobTitle,
                                String reason, String jobUrl) {
        String name = safeName(recipientName);
        String subject = "Update on your job posting: " + jobTitle;
        String text = """
                Hi %s,

                Your job posting "%s" was not approved by the placement cell.

                Reason: %s

                You can review and repost: %s

                — %s""".formatted(name, jobTitle, reason, jobUrl, properties.fromName());
        String html = htmlTemplate("Update on your job posting",
                "Job not approved",
                "Your posting \"" + escape(jobTitle) + "\" was not approved. Reason: " + escape(reason),
                "Review posting", jobUrl,
                "You are receiving this because you posted a job on CampusConnect.");
        dispatch(new EmailMessage(toEmail, name, subject, html, text, "job-rejected"));
    }

    @Override
    @Async
    public void sendNewEligibleJob(String toEmail, String recipientName, String jobTitle,
                                   String companyName, String jobUrl) {
        String name = safeName(recipientName);
        String subject = "New opportunity: " + jobTitle + " at " + companyName;
        String text = """
                Hi %s,

                A new job matches your profile: "%s" at %s.

                View and apply: %s

                — %s""".formatted(name, jobTitle, companyName, jobUrl, properties.fromName());
        String html = htmlTemplate("A new job matches your profile",
                "New opportunity for you",
                "A new job matches your profile: \"" + escape(jobTitle) + "\" at " + escape(companyName) + ".",
                "View and apply", jobUrl,
                "You are receiving this because it matches your placement profile.");
        dispatch(new EmailMessage(toEmail, name, subject, html, text, "new-eligible-job"));
    }

    @Override
    @Async
    public void sendApplicationReceived(String toEmail, String recipientName, String studentName,
                                        String jobTitle, String applicationUrl) {
        String name = safeName(recipientName);
        String subject = "New application for " + jobTitle;
        String text = """
                Hi %s,

                %s has applied to your job posting "%s".

                Review the application: %s

                — %s""".formatted(name, studentName, jobTitle, applicationUrl, properties.fromName());
        String html = htmlTemplate("New application received",
                "New application",
                escape(studentName) + " has applied to your posting \"" + escape(jobTitle) + "\".",
                "Review application", applicationUrl,
                "You are receiving this because you posted a job on CampusConnect.");
        dispatch(new EmailMessage(toEmail, name, subject, html, text, "application-received"));
    }

    // ---------------------------------------------------------------- internals

    /**
     * Sends the message and records the outcome. Invoked on an async thread with no ambient
     * transaction; the single {@code save} is atomic on its own, so a mail failure only affects
     * this log row and never the business transaction that already committed.
     */
    private void dispatch(EmailMessage message) {
        EmailLog logEntry = EmailLog.builder()
                .recipientEmail(message.toEmail())
                .subject(message.subject())
                .template(message.template())
                .status(EmailLog.Status.PENDING)
                .attempts(1)
                .build();
        try {
            String messageId = sender.send(message);
            logEntry.setStatus(EmailLog.Status.SENT);
            logEntry.setProviderMessageId(messageId);
            logEntry.setSentAt(Instant.now());
            log.debug("Email '{}' sent to {} via {} (id={})",
                    message.template(), message.toEmail(), sender.provider(), messageId);
        } catch (Exception ex) {
            logEntry.setStatus(EmailLog.Status.FAILED);
            logEntry.setErrorMessage(truncate(ex.getMessage()));
            log.error("Failed to send email '{}' to {} via {}: {}",
                    message.template(), message.toEmail(), sender.provider(), ex.getMessage(), ex);
        }
        emailLogRepository.save(logEntry);
    }

    private String safeName(String name) {
        return (name == null || name.isBlank()) ? "there" : name;
    }

    private String truncate(String value) {
        if (value == null) {
            return "unknown error";
        }
        return value.length() > 1000 ? value.substring(0, 1000) : value;
    }

    private String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String htmlTemplate(String preheader, String heading, String intro,
                                String ctaLabel, String ctaUrl, String footer) {
        Map<String, String> tokens = Map.of(
                "preheader", escape(preheader),
                "heading", escape(heading),
                "intro", escape(intro),
                "ctaLabel", escape(ctaLabel),
                "ctaUrl", ctaUrl,
                "footer", escape(footer),
                "brand", escape(properties.fromName()));
        String template = """
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="utf-8"><title>${preheader}</title></head>
                <body style="margin:0;padding:0;background:#f4f5f7;font-family:Arial,Helvetica,sans-serif;">
                  <div style="max-width:560px;margin:0 auto;padding:32px 24px;">
                    <div style="background:#ffffff;border-radius:12px;padding:32px;">
                      <h1 style="font-size:20px;color:#1a1a2e;margin:0 0 16px;">${heading}</h1>
                      <p style="font-size:15px;color:#444;line-height:1.5;margin:0 0 24px;">${intro}</p>
                      <a href="${ctaUrl}" style="display:inline-block;background:#3454d1;color:#ffffff;
                         text-decoration:none;padding:12px 28px;border-radius:8px;font-size:15px;">${ctaLabel}</a>
                      <p style="font-size:13px;color:#888;line-height:1.5;margin:24px 0 0;">${footer}</p>
                      <p style="font-size:12px;color:#aaa;margin:24px 0 0;">${brand}</p>
                    </div>
                  </div>
                </body>
                </html>""";
        return substitute(template, tokens);
    }

    /** Variant of {@link #htmlTemplate} that displays a one-time code prominently instead of a CTA link. */
    private String otpTemplate(String preheader, String heading, String intro, String code, String footer) {
        Map<String, String> tokens = Map.of(
                "preheader", escape(preheader),
                "heading", escape(heading),
                "intro", escape(intro),
                "code", escape(code),
                "footer", escape(footer),
                "brand", escape(properties.fromName()));
        String template = """
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="utf-8"><title>${preheader}</title></head>
                <body style="margin:0;padding:0;background:#f4f5f7;font-family:Arial,Helvetica,sans-serif;">
                  <div style="max-width:560px;margin:0 auto;padding:32px 24px;">
                    <div style="background:#ffffff;border-radius:12px;padding:32px;">
                      <h1 style="font-size:20px;color:#1a1a2e;margin:0 0 16px;">${heading}</h1>
                      <p style="font-size:15px;color:#444;line-height:1.5;margin:0 0 24px;">${intro}</p>
                      <div style="font-size:34px;font-weight:bold;letter-spacing:10px;color:#1a1a2e;
                         background:#f0f2fb;border-radius:8px;padding:18px 0;text-align:center;">${code}</div>
                      <p style="font-size:13px;color:#888;line-height:1.5;margin:24px 0 0;">${footer}</p>
                      <p style="font-size:12px;color:#aaa;margin:24px 0 0;">${brand}</p>
                    </div>
                  </div>
                </body>
                </html>""";
        return substitute(template, tokens);
    }

    private String substitute(String template, Map<String, String> tokens) {
        String result = template;
        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}",
                    entry.getValue() == null ? "" : entry.getValue());
        }
        return result;
    }
}
