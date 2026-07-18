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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Student profile — the subject of the eligibility engine. Every field consumed by
 * {@code EligibilityEngineService} lives here so eligibility is a pure function of
 * (Student, Job). Backed 1:1 by a {@link User} with role {@code STUDENT}.
 */
@Entity
@Table(name = "students",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_students_user", columnNames = "user_id"),
                @UniqueConstraint(name = "uk_students_roll", columnNames = {"university_id", "roll_number"})
        },
        indexes = {
                @Index(name = "idx_students_university", columnList = "university_id"),
                @Index(name = "idx_students_dept_branch", columnList = "department,branch"),
                @Index(name = "idx_students_passing_year", columnList = "passing_year"),
                @Index(name = "idx_students_cgpa", columnList = "cgpa"),
                @Index(name = "idx_students_placement_eligible", columnList = "placement_eligible")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_students_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "university_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_students_university"))
    private University university;

    @Column(name = "roll_number", nullable = false, length = 40)
    private String rollNumber;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "branch", nullable = false, length = 100)
    private String branch;

    @Column(name = "degree", length = 60)
    private String degree;

    /** CGPA on a 0.00–10.00 scale, precision-safe for comparisons. */
    @Column(name = "cgpa", nullable = false, precision = 4, scale = 2)
    private BigDecimal cgpa;

    @Column(name = "active_backlogs", nullable = false)
    @Builder.Default
    private int activeBacklogs = 0;

    @Column(name = "total_backlogs", nullable = false)
    @Builder.Default
    private int totalBacklogs = 0;

    @Column(name = "passing_year", nullable = false)
    private int passingYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_authorization", nullable = false, length = 30)
    @Builder.Default
    private WorkAuthorization workAuthorization = WorkAuthorization.CITIZEN;

    @Column(name = "location", length = 120)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    @Builder.Default
    private Gender gender = Gender.UNDISCLOSED;

    /** Used to derive age for age-based eligibility rules. Null = age is unconstrained-friendly. */
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "resume_url", length = 512)
    private String resumeUrl;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    /**
     * Master switch controlled by the placement cell. Even a profile that matches a job's
     * criteria is excluded from eligibility when this is false (e.g. already placed, debarred).
     */
    @Column(name = "placement_eligible", nullable = false)
    @Builder.Default
    private boolean placementEligible = true;

    /** Normalised (lower-cased) skill tokens for set-based eligibility matching. */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "student_skills",
            joinColumns = @JoinColumn(name = "student_id",
                    foreignKey = @ForeignKey(name = "fk_student_skills_student")),
            uniqueConstraints = @UniqueConstraint(name = "uk_student_skills",
                    columnNames = {"student_id", "skill"}))
    @Column(name = "skill", length = 80, nullable = false)
    @Builder.Default
    private Set<String> skills = new HashSet<>();
}
