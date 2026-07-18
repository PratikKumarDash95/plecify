package com.campusconnect.portal.entity;

import com.campusconnect.portal.common.enums.JobStatus;
import com.campusconnect.portal.common.enums.JobType;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A job posting created by a {@link Company}. Enters as {@link JobStatus#PENDING}, is
 * reviewed by a {@link PlacementCell}, and only becomes visible to students (via
 * {@code eligible_jobs}) once {@link JobStatus#APPROVED}. Eligibility rules are held in the
 * associated {@link JobEligibility} record.
 */
@Entity
@Table(name = "jobs",
        indexes = {
                @Index(name = "idx_jobs_status", columnList = "status"),
                @Index(name = "idx_jobs_company", columnList = "company_id"),
                @Index(name = "idx_jobs_placement_cell", columnList = "placement_cell_id"),
                @Index(name = "idx_jobs_deadline", columnList = "application_deadline"),
                @Index(name = "idx_jobs_status_deadline", columnList = "status,application_deadline")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_jobs_company"))
    private Company company;

    /**
     * The placement cell responsible for reviewing this job (the target campus's cell).
     * Set when the company scopes the posting to a university.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "placement_cell_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_jobs_placement_cell"))
    private PlacementCell placementCell;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 30)
    private JobType jobType;

    @Column(name = "location", length = 150)
    private String location;

    @Column(name = "remote_allowed", nullable = false)
    @Builder.Default
    private boolean remoteAllowed = false;

    @Column(name = "salary_min", precision = 12, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 12, scale = 2)
    private BigDecimal salaryMax;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "openings", nullable = false)
    @Builder.Default
    private int openings = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private JobStatus status = JobStatus.PENDING;

    @Column(name = "application_deadline", nullable = false)
    private Instant applicationDeadline;

    /** Populated when the placement cell rejects the posting. */
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    /** The placement-cell user id that approved this posting. Null until approved. */
    @Column(name = "approved_by")
    private UUID approvedBy;

    /** When the posting was approved. Null until approved. */
    @Column(name = "approved_at")
    private Instant approvedAt;

    /** Timestamp the eligibility engine last materialised results for this job. */
    @Column(name = "eligibility_computed_at")
    private Instant eligibilityComputedAt;

    @OneToOne(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true, optional = false,
            fetch = FetchType.LAZY)
    private JobEligibility eligibility;

    public void setEligibility(JobEligibility eligibility) {
        this.eligibility = eligibility;
        if (eligibility != null) {
            eligibility.setJob(this);
        }
    }

    public boolean isOpenForApplications() {
        return status == JobStatus.APPROVED
                && applicationDeadline != null
                && applicationDeadline.isAfter(Instant.now());
    }
}
