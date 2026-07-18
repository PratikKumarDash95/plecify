package com.campusconnect.portal.entity;

import com.campusconnect.portal.common.enums.ApplicationStatus;
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
 * A student's application to a job. Uniqueness on (student, job) prevents duplicate
 * applications. Status transitions are governed by {@link ApplicationStatus}'s state machine.
 */
@Entity
@Table(name = "applications",
        uniqueConstraints = @UniqueConstraint(name = "uk_applications_student_job",
                columnNames = {"student_id", "job_id"}),
        indexes = {
                @Index(name = "idx_applications_job_status", columnList = "job_id,status"),
                @Index(name = "idx_applications_student", columnList = "student_id"),
                @Index(name = "idx_applications_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_applications_student"))
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_applications_job"))
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    /** Snapshot of the resume URL used for this application (may differ from current profile). */
    @Column(name = "resume_url", length = 512)
    private String resumeUrl;

    @Column(name = "cover_letter", columnDefinition = "text")
    private String coverLetter;

    /** Scheduled interview time, set when status moves to INTERVIEW_SCHEDULED. */
    @Column(name = "interview_at")
    private Instant interviewAt;

    @Column(name = "interview_details", length = 1000)
    private String interviewDetails;

    /** Free-text note attached on the most recent status change (e.g. rejection reason). */
    @Column(name = "status_note", length = 1000)
    private String statusNote;

    @Column(name = "last_status_change_at")
    private Instant lastStatusChangeAt;
}
