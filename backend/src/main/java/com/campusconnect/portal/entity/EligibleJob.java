package com.campusconnect.portal.entity;

import com.campusconnect.portal.common.enums.EligibleJobStatus;
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
 * Materialised output of the eligibility engine: one row per (student, job) the engine
 * deemed eligible. The student dashboard reads directly from this table instead of
 * recomputing eligibility — the mandatory pre-computation requirement.
 *
 * <p>A unique (student, job) constraint makes engine re-runs idempotent.
 */
@Entity
@Table(name = "eligible_jobs",
        uniqueConstraints = @UniqueConstraint(name = "uk_eligible_jobs_student_job",
                columnNames = {"student_id", "job_id"}),
        indexes = {
                @Index(name = "idx_eligible_jobs_student_status", columnList = "student_id,status"),
                @Index(name = "idx_eligible_jobs_job", columnList = "job_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EligibleJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_eligible_jobs_student"))
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_eligible_jobs_job"))
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EligibleJobStatus status = EligibleJobStatus.ELIGIBLE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @jakarta.persistence.PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @jakarta.persistence.PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
