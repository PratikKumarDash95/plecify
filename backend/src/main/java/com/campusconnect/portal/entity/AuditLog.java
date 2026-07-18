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
 * Immutable security/audit record of a significant action (login, job approval, status
 * change, role grant, etc.). Written asynchronously so it never blocks the request path.
 */
@Entity
@Table(name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_logs_actor", columnList = "actor_email,created_at"),
                @Index(name = "idx_audit_logs_action", columnList = "action,created_at"),
                @Index(name = "idx_audit_logs_target", columnList = "target_type,target_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "actor_email", length = 180)
    private String actorEmail;

    @Column(name = "actor_id")
    private UUID actorId;

    /** Action code, e.g. {@code JOB_APPROVED}, {@code USER_LOGIN}, {@code APPLICATION_STATUS_CHANGED}. */
    @Column(name = "action", nullable = false, length = 80)
    private String action;

    @Column(name = "target_type", length = 60)
    private String targetType;

    @Column(name = "target_id", length = 80)
    private String targetId;

    @Column(name = "details", columnDefinition = "text")
    private String details;

    @Column(name = "ip_address", length = 60)
    private String ipAddress;

    @Column(name = "success", nullable = false)
    @Builder.Default
    private boolean success = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
