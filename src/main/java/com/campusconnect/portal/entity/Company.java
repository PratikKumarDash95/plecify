package com.campusconnect.portal.entity;

import com.campusconnect.portal.common.enums.ApprovalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Recruiter/company profile. Backed 1:1 by a {@link User} with role {@code COMPANY}.
 * Must be {@link ApprovalStatus#APPROVED} by an admin before it can post jobs.
 */
@Entity
@Table(name = "companies",
        uniqueConstraints = @UniqueConstraint(name = "uk_companies_user", columnNames = "user_id"),
        indexes = {
                @Index(name = "idx_companies_status", columnList = "status"),
                @Index(name = "idx_companies_name", columnList = "name")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_companies_user"))
    private User user;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "industry", length = 120)
    private String industry;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    /** URL only; the binary lives in Supabase storage. */
    @Column(name = "logo_url", length = 512)
    private String logoUrl;

    @Column(name = "headquarters", length = 200)
    private String headquarters;

    @Column(name = "contact_email", length = 180)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ApprovalStatus status = ApprovalStatus.PENDING;

    public boolean isApproved() {
        return status == ApprovalStatus.APPROVED;
    }
}
