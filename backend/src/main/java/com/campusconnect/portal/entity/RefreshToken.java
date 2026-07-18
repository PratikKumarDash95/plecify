package com.campusconnect.portal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Server-side record of an issued refresh token, enabling rotation and revocation
 * (logout, "log out everywhere"). Only a SHA-256 hash of the token is stored — the raw
 * value is never persisted. Rotation is chained via {@link #replacedByTokenHash}.
 */
@Entity
@Table(name = "refresh_tokens",
        uniqueConstraints = @UniqueConstraint(name = "uk_refresh_tokens_hash", columnNames = "token_hash"),
        indexes = {
                @Index(name = "idx_refresh_tokens_user", columnList = "user_id"),
                @Index(name = "idx_refresh_tokens_expires", columnList = "expires_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_refresh_tokens_user"))
    private User user;

    @Column(name = "token_hash", nullable = false, length = 100)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private boolean revoked = false;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_token_hash", length = 100)
    private String replacedByTokenHash;

    @Column(name = "user_agent", length = 300)
    private String userAgent;

    @Column(name = "ip_address", length = 60)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public boolean isActive() {
        return !revoked && expiresAt.isAfter(Instant.now());
    }

    public void revoke() {
        this.revoked = true;
        this.revokedAt = Instant.now();
    }
}
