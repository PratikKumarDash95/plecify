package com.campusconnect.portal.service.email;

/**
 * Immutable, provider-agnostic representation of a composed email ready to send.
 *
 * @param toEmail   recipient address
 * @param toName    recipient display name
 * @param subject   subject line
 * @param htmlBody  HTML body
 * @param textBody  plain-text fallback body
 * @param template  logical template name, recorded for auditing
 */
public record EmailMessage(
        String toEmail,
        String toName,
        String subject,
        String htmlBody,
        String textBody,
        String template
) {
}
