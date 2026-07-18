package com.campusconnect.portal.entity;

import com.campusconnect.portal.common.enums.Gender;
import com.campusconnect.portal.common.enums.WorkAuthorization;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Declarative eligibility rules attached to a {@link Job}. Every field is optional; a null
 * (or empty collection) means "no constraint on this dimension". The deterministic
 * {@code EligibilityEngineService} reads these rules to decide student eligibility.
 *
 * <p>Skills matching supports {@link #skillMatchMode}: {@code ALL} (student must have every
 * required skill) or {@code ANY} (at least one).
 */
@Entity
@Table(name = "job_eligibility",
        uniqueConstraints = @UniqueConstraint(name = "uk_job_eligibility_job", columnNames = "job_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobEligibility {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_job_eligibility_job"))
    private Job job;

    /** Minimum CGPA (inclusive) on a 0–10 scale. Null = no minimum. */
    @Column(name = "min_cgpa", precision = 4, scale = 2)
    private BigDecimal minCgpa;

    /** Maximum active backlogs allowed (inclusive). Null = no limit. */
    @Column(name = "max_active_backlogs")
    private Integer maxActiveBacklogs;

    /** Maximum total (historic) backlogs allowed (inclusive). Null = no limit. */
    @Column(name = "max_total_backlogs")
    private Integer maxTotalBacklogs;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_work_authorization", length = 30)
    @Builder.Default
    private WorkAuthorization requiredWorkAuthorization = WorkAuthorization.ANY;

    public enum SkillMatchMode { ALL, ANY }

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_match_mode", length = 10)
    @Builder.Default
    private SkillMatchMode skillMatchMode = SkillMatchMode.ANY;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_eligible_departments",
            joinColumns = @JoinColumn(name = "job_eligibility_id",
                    foreignKey = @ForeignKey(name = "fk_job_elig_departments")))
    @Column(name = "department", length = 100)
    @Builder.Default
    private Set<String> departments = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_eligible_branches",
            joinColumns = @JoinColumn(name = "job_eligibility_id",
                    foreignKey = @ForeignKey(name = "fk_job_elig_branches")))
    @Column(name = "branch", length = 100)
    @Builder.Default
    private Set<String> branches = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_eligible_passing_years",
            joinColumns = @JoinColumn(name = "job_eligibility_id",
                    foreignKey = @ForeignKey(name = "fk_job_elig_years")))
    @Column(name = "passing_year")
    @Builder.Default
    private Set<Integer> passingYears = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_required_skills",
            joinColumns = @JoinColumn(name = "job_eligibility_id",
                    foreignKey = @ForeignKey(name = "fk_job_elig_skills")))
    @Column(name = "skill", length = 80)
    @Builder.Default
    private Set<String> requiredSkills = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_eligible_locations",
            joinColumns = @JoinColumn(name = "job_eligibility_id",
                    foreignKey = @ForeignKey(name = "fk_job_elig_locations")))
    @Column(name = "location", length = 120)
    @Builder.Default
    private Set<String> allowedLocations = new HashSet<>();

    /**
     * Genders eligible for this posting. Empty = no gender constraint (the common case).
     * Populated only for diversity-scoped drives.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_eligible_genders",
            joinColumns = @JoinColumn(name = "job_eligibility_id",
                    foreignKey = @ForeignKey(name = "fk_job_elig_genders")))
    @Column(name = "gender", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Gender> allowedGenders = new HashSet<>();

    /** Batch labels (e.g. "2025", "2026-Spring") eligible for the drive. Empty = no batch constraint. */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "job_eligible_batches",
            joinColumns = @JoinColumn(name = "job_eligibility_id",
                    foreignKey = @ForeignKey(name = "fk_job_elig_batches")))
    @Column(name = "batch", length = 40)
    @Builder.Default
    private Set<String> batches = new HashSet<>();

    /** Minimum age in years (inclusive), evaluated against the student's date of birth. Null = no minimum. */
    @Column(name = "min_age")
    private Integer minAge;

    /** Maximum age in years (inclusive). Null = no maximum. */
    @Column(name = "max_age")
    private Integer maxAge;

    /**
     * Minimum annual package/CTC the offer must meet for the student to be considered eligible,
     * compared against the job's {@code salaryMax}. Null = no package floor. This models
     * "package eligibility" drives where students above a CTC threshold are barred (or required).
     */
    @Column(name = "min_package", precision = 12, scale = 2)
    private BigDecimal minPackage;

    /** Maximum annual package/CTC allowed, compared against the job's {@code salaryMin}. Null = no ceiling. */
    @Column(name = "max_package", precision = 12, scale = 2)
    private BigDecimal maxPackage;
}
