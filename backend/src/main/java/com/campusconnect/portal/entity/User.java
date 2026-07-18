package com.campusconnect.portal.entity;

import com.campusconnect.portal.common.enums.AuthProvider;
import com.campusconnect.portal.common.enums.RoleType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Central identity record. One {@code User} owns exactly one role-specific profile
 * ({@link Student}, {@link Company}, or {@link PlacementCell}); ADMIN users have none.
 * Authentication credentials and account flags live here, not in the profile tables.
 */
@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_enabled", columnList = "enabled")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", nullable = false, length = 180)
    private String email;

    // Nullable: federated accounts (e.g. Google) authenticate via their provider and have no
    // local password. Guard credential checks with hasPassword() before using this.
    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    /** The external provider's stable subject id (e.g. Google {@code sub}); null for LOCAL accounts. */
    @Column(name = "provider_id", length = 120)
    private String providerId;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "account_locked", nullable = false)
    @Builder.Default
    private boolean accountLocked = false;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_user_roles",
                    columnNames = {"user_id", "role_id"}))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public boolean hasRole(RoleType roleType) {
        return roles.stream().anyMatch(r -> r.getName() == roleType);
    }

    /** True when a local password is set; federated-only accounts return false. */
    public boolean hasPassword() {
        return passwordHash != null && !passwordHash.isBlank();
    }
}
