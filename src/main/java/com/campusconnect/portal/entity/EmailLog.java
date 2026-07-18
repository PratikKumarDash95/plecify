package com.campusconnect.portal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Delivery record for every outbound email. Enables auditing, retries, and debugging of the
 * Brevo integration without leaking full bodies (we store the template + a truncated context).
 */
@Entity
@Table(name = "email_logs",
        indexes = {
                @Index(name = "idx_email_logs_recipient", columnList = "recipient_email,created_at"),
                @Index(name = "idx_email_logs_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog {

    public enum Status { PENDING, SENT, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "recipient_email", nullable = false, length = 180)
    private String recipientEmail;

    @Column(name = "subject", nullable = false, length = 300)
    private String subject;

    @Column(name = "template", length = 80)
    private String template;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.PENDING;

    /** Provider message id (Brevo) when accepted. */
    @Column(name = "provider_message_id", length = 200)
    private String providerMessageId;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private int attempts = 0;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
