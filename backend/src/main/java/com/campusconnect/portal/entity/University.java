package com.campusconnect.portal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * A tenant university. Placement cells and students belong to a university; this is the
 * anchor for multi-tenant isolation across 100+ campuses.
 */
@Entity
@Table(name = "universities",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_universities_code", columnNames = "code"),
                @UniqueConstraint(name = "uk_universities_domain", columnNames = "email_domain")
        },
        indexes = @Index(name = "idx_universities_code", columnList = "code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class University extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /** Short unique code, e.g. {@code MIT}, {@code IITB}. */
    @Column(name = "code", nullable = false, length = 30)
    private String code;

    /** Institutional email domain used to auto-associate student signups, e.g. {@code mit.edu}. */
    @Column(name = "email_domain", length = 120)
    private String emailDomain;

    @Column(name = "city", length = 120)
    private String city;

    @Column(name = "country", length = 120)
    private String country;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
