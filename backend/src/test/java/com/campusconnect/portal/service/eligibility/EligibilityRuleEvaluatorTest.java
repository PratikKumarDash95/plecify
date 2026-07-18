package com.campusconnect.portal.service.eligibility;

import com.campusconnect.portal.common.enums.Gender;
import com.campusconnect.portal.common.enums.WorkAuthorization;
import com.campusconnect.portal.entity.Job;
import com.campusconnect.portal.entity.JobEligibility;
import com.campusconnect.portal.entity.JobEligibility.SkillMatchMode;
import com.campusconnect.portal.entity.Student;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exhaustive tests for the deterministic eligibility rule evaluator. Each nested class
 * isolates a single dimension so a regression points straight at the offending rule. The
 * evaluation date is fixed so age math is reproducible.
 */
class EligibilityRuleEvaluatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 1, 15);

    private final EligibilityRuleEvaluator evaluator = new EligibilityRuleEvaluator();

    // ---------------------------------------------------------------- fixtures

    private Student student() {
        Student s = new Student();
        s.setCgpa(new BigDecimal("8.00"));
        s.setActiveBacklogs(0);
        s.setTotalBacklogs(0);
        s.setDepartment("Computer Science");
        s.setBranch("CSE");
        s.setPassingYear(2026);
        s.setWorkAuthorization(WorkAuthorization.CITIZEN);
        s.setLocation("Bengaluru");
        s.setGender(Gender.FEMALE);
        s.setDateOfBirth(LocalDate.of(2004, 1, 1)); // age 22 on TODAY
        s.setSkills(new HashSet<>(Set.of("java", "spring")));
        return s;
    }

    private Job jobWith(JobEligibility rules) {
        Job job = new Job();
        job.setSalaryMin(new BigDecimal("800000"));
        job.setSalaryMax(new BigDecimal("1500000"));
        if (rules != null) {
            job.setEligibility(rules); // wires job<->rules bidirectionally
        }
        return job;
    }

    private JobEligibility emptyRules() {
        return JobEligibility.builder().build();
    }

    // ---------------------------------------------------------------- no rules

    @Test
    void nullEligibility_isOpenToAll() {
        Job job = new Job();
        assertThat(evaluator.isEligible(job, student(), TODAY)).isTrue();
    }

    @Test
    void emptyRules_matchEveryStudent() {
        assertThat(evaluator.isEligible(jobWith(emptyRules()), student(), TODAY)).isTrue();
    }

    // ---------------------------------------------------------------- CGPA

    @Nested
    class Cgpa {
        @Test
        void aboveMinimum_passes() {
            JobEligibility r = JobEligibility.builder().minCgpa(new BigDecimal("7.50")).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isTrue();
        }

        @Test
        void exactlyAtMinimum_passes() {
            JobEligibility r = JobEligibility.builder().minCgpa(new BigDecimal("8.00")).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isTrue();
        }

        @Test
        void belowMinimum_fails() {
            JobEligibility r = JobEligibility.builder().minCgpa(new BigDecimal("8.01")).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isFalse();
        }
    }

    // ---------------------------------------------------------------- backlogs

    @Nested
    class Backlogs {
        @Test
        void withinActiveLimit_passes() {
            Student s = student();
            s.setActiveBacklogs(1);
            JobEligibility r = JobEligibility.builder().maxActiveBacklogs(1).build();
            assertThat(evaluator.isEligible(jobWith(r), s, TODAY)).isTrue();
        }

        @Test
        void exceedsActiveLimit_fails() {
            Student s = student();
            s.setActiveBacklogs(2);
            JobEligibility r = JobEligibility.builder().maxActiveBacklogs(1).build();
            assertThat(evaluator.isEligible(jobWith(r), s, TODAY)).isFalse();
        }

        @Test
        void exceedsTotalLimit_fails() {
            Student s = student();
            s.setTotalBacklogs(5);
            JobEligibility r = JobEligibility.builder().maxTotalBacklogs(2).build();
            assertThat(evaluator.isEligible(jobWith(r), s, TODAY)).isFalse();
        }
    }

    // ---------------------------------------------------------------- work auth

    @Nested
    class WorkAuth {
        @Test
        void anyRequirement_passesRegardlessOfStudent() {
            JobEligibility r = JobEligibility.builder()
                    .requiredWorkAuthorization(WorkAuthorization.ANY).build();
            Student s = student();
            s.setWorkAuthorization(WorkAuthorization.REQUIRES_SPONSORSHIP);
            assertThat(evaluator.isEligible(jobWith(r), s, TODAY)).isTrue();
        }

        @Test
        void matchingRequirement_passes() {
            JobEligibility r = JobEligibility.builder()
                    .requiredWorkAuthorization(WorkAuthorization.CITIZEN).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isTrue();
        }

        @Test
        void mismatchedRequirement_fails() {
            JobEligibility r = JobEligibility.builder()
                    .requiredWorkAuthorization(WorkAuthorization.CITIZEN).build();
            Student s = student();
            s.setWorkAuthorization(WorkAuthorization.REQUIRES_SPONSORSHIP);
            assertThat(evaluator.isEligible(jobWith(r), s, TODAY)).isFalse();
        }
    }

    // ---------------------------------------------------------------- dept / branch / year / location

    @Nested
    class SetMembership {
        @Test
        void department_caseInsensitiveMatch_passes() {
            JobEligibility r = JobEligibility.builder()
                    .departments(new HashSet<>(Set.of("computer science"))).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isTrue();
        }

        @Test
        void department_notInSet_fails() {
            JobEligibility r = JobEligibility.builder()
                    .departments(new HashSet<>(Set.of("Mechanical"))).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isFalse();
        }

        @Test
        void branch_notInSet_fails() {
            JobEligibility r = JobEligibility.builder()
                    .branches(new HashSet<>(Set.of("ECE"))).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isFalse();
        }

        @Test
        void passingYear_inSet_passes() {
            JobEligibility r = JobEligibility.builder()
                    .passingYears(new HashSet<>(Set.of(2025, 2026))).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isTrue();
        }

        @Test
        void passingYear_notInSet_fails() {
            JobEligibility r = JobEligibility.builder()
                    .passingYears(new HashSet<>(Set.of(2024, 2025))).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isFalse();
        }

        @Test
        void location_notInSet_fails() {
            JobEligibility r = JobEligibility.builder()
                    .allowedLocations(new HashSet<>(Set.of("Mumbai"))).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isFalse();
        }

        @Test
        void location_nullStudentValueWithConstraint_fails() {
            JobEligibility r = JobEligibility.builder()
                    .allowedLocations(new HashSet<>(Set.of("Mumbai"))).build();
            Student s = student();
            s.setLocation(null);
            assertThat(evaluator.isEligible(jobWith(r), s, TODAY)).isFalse();
        }
    }

    // ---------------------------------------------------------------- gender

    @Nested
    class GenderRule {
        @Test
        void inAllowedSet_passes() {
            JobEligibility r = JobEligibility.builder()
                    .allowedGenders(new HashSet<>(Set.of(Gender.FEMALE, Gender.OTHER))).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isTrue();
        }

        @Test
        void notInAllowedSet_fails() {
            JobEligibility r = JobEligibility.builder()
                    .allowedGenders(new HashSet<>(Set.of(Gender.MALE))).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isFalse();
        }
    }

    // ---------------------------------------------------------------- age

    @Nested
    class Age {
        @Test
        void withinRange_passes() {
            JobEligibility r = JobEligibility.builder().minAge(18).maxAge(27).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isTrue();
        }

        @Test
        void belowMinAge_fails() {
            JobEligibility r = JobEligibility.builder().minAge(25).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isFalse();
        }

        @Test
        void aboveMaxAge_fails() {
            JobEligibility r = JobEligibility.builder().maxAge(21).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isFalse();
        }

        @Test
        void ruleWithNoDateOfBirth_fails() {
            JobEligibility r = JobEligibility.builder().minAge(18).build();
            Student s = student();
            s.setDateOfBirth(null);
            assertThat(evaluator.isEligible(jobWith(r), s, TODAY)).isFalse();
        }

        @Test
        void noAgeRule_ignoresMissingDateOfBirth() {
            Student s = student();
            s.setDateOfBirth(null);
            assertThat(evaluator.isEligible(jobWith(emptyRules()), s, TODAY)).isTrue();
        }
    }

    // ---------------------------------------------------------------- skills

    @Nested
    class Skills {
        @Test
        void anyMode_matchesOnOneSkill() {
            JobEligibility r = JobEligibility.builder()
                    .skillMatchMode(SkillMatchMode.ANY)
                    .requiredSkills(new HashSet<>(Set.of("java", "python"))).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isTrue();
        }

        @Test
        void anyMode_failsWhenNoOverlap() {
            JobEligibility r = JobEligibility.builder()
                    .skillMatchMode(SkillMatchMode.ANY)
                    .requiredSkills(new HashSet<>(Set.of("go", "rust"))).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isFalse();
        }

        @Test
        void allMode_passesWhenStudentHasEvery() {
            JobEligibility r = JobEligibility.builder()
                    .skillMatchMode(SkillMatchMode.ALL)
                    .requiredSkills(new HashSet<>(Set.of("java", "spring"))).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isTrue();
        }

        @Test
        void allMode_failsWhenMissingOne() {
            JobEligibility r = JobEligibility.builder()
                    .skillMatchMode(SkillMatchMode.ALL)
                    .requiredSkills(new HashSet<>(Set.of("java", "spring", "kafka"))).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isFalse();
        }

        @Test
        void caseInsensitiveMatching() {
            JobEligibility r = JobEligibility.builder()
                    .skillMatchMode(SkillMatchMode.ALL)
                    .requiredSkills(new HashSet<>(Set.of("JAVA", "Spring"))).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isTrue();
        }

        @Test
        void nullSkillMatchMode_defaultsToAny() {
            JobEligibility r = JobEligibility.builder()
                    .skillMatchMode(null)
                    .requiredSkills(new HashSet<>(Set.of("java", "python"))).build();
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isTrue();
        }
    }

    // ---------------------------------------------------------------- package

    @Nested
    class PackageBand {
        @Test
        void jobMeetsMinPackage_passes() {
            JobEligibility r = JobEligibility.builder().minPackage(new BigDecimal("1000000")).build();
            // job salaryMax = 1,500,000 >= 1,000,000
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isTrue();
        }

        @Test
        void jobBelowMinPackage_fails() {
            JobEligibility r = JobEligibility.builder().minPackage(new BigDecimal("2000000")).build();
            // job salaryMax = 1,500,000 < 2,000,000
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isFalse();
        }

        @Test
        void jobWithinMaxPackage_passes() {
            JobEligibility r = JobEligibility.builder().maxPackage(new BigDecimal("1000000")).build();
            // job salaryMin = 800,000 <= 1,000,000
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isTrue();
        }

        @Test
        void jobExceedsMaxPackage_fails() {
            JobEligibility r = JobEligibility.builder().maxPackage(new BigDecimal("500000")).build();
            // job salaryMin = 800,000 > 500,000
            assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isFalse();
        }
    }

    // ---------------------------------------------------------------- combined

    @Test
    void allDimensionsSatisfied_passes() {
        JobEligibility r = JobEligibility.builder()
                .minCgpa(new BigDecimal("7.00"))
                .maxActiveBacklogs(0)
                .maxTotalBacklogs(1)
                .requiredWorkAuthorization(WorkAuthorization.CITIZEN)
                .departments(new HashSet<>(Set.of("Computer Science")))
                .branches(new HashSet<>(Set.of("CSE")))
                .passingYears(new HashSet<>(Set.of(2026)))
                .allowedLocations(new HashSet<>(Set.of("Bengaluru")))
                .allowedGenders(new HashSet<>(Set.of(Gender.FEMALE)))
                .minAge(18).maxAge(30)
                .skillMatchMode(SkillMatchMode.ALL)
                .requiredSkills(new HashSet<>(Set.of("java", "spring")))
                .minPackage(new BigDecimal("1000000"))
                .maxPackage(new BigDecimal("2000000"))
                .build();
        assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isTrue();
    }

    @Test
    void oneFailingDimension_failsWhole() {
        JobEligibility r = JobEligibility.builder()
                .minCgpa(new BigDecimal("7.00"))
                .branches(new HashSet<>(Set.of("ECE"))) // student is CSE → fails
                .build();
        assertThat(evaluator.isEligible(jobWith(r), student(), TODAY)).isFalse();
    }
}
