package com.campusconnect.portal.service.eligibility;

import com.campusconnect.portal.common.enums.Gender;
import com.campusconnect.portal.common.enums.WorkAuthorization;
import com.campusconnect.portal.entity.Job;
import com.campusconnect.portal.entity.JobEligibility;
import com.campusconnect.portal.entity.JobEligibility.SkillMatchMode;
import com.campusconnect.portal.entity.Student;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pure, side-effect-free evaluation of a job's eligibility rules against a single student.
 * Every rule is a conjunction (AND): a student is eligible only if they satisfy every
 * declared constraint. A null scalar or empty collection means "no constraint on that
 * dimension" and always passes. The method is deterministic — identical inputs always yield
 * the same result — which is what makes engine re-runs reproducible.
 */
@Component
public class EligibilityRuleEvaluator {

    /**
     * @param job       the (approved) job whose {@link JobEligibility} rules are evaluated;
     *                  the job's own salary range is used for package-band checks
     * @param student   the candidate; skills must be initialised by the caller
     * @param today     evaluation date, injected for deterministic age calculation
     * @return {@code true} iff the student satisfies every declared rule
     */
    public boolean isEligible(Job job, Student student, LocalDate today) {
        JobEligibility rules = job.getEligibility();
        if (rules == null) {
            return true; // no rules declared → open to all placement-eligible students
        }

        return cgpaOk(rules, student)
                && backlogsOk(rules, student)
                && workAuthOk(rules, student)
                && departmentOk(rules, student)
                && branchOk(rules, student)
                && passingYearOk(rules, student)
                && locationOk(rules, student)
                && genderOk(rules, student)
                && ageOk(rules, student, today)
                && skillsOk(rules, student)
                && packageOk(rules, job);
    }

    private boolean cgpaOk(JobEligibility rules, Student student) {
        BigDecimal min = rules.getMinCgpa();
        return min == null || (student.getCgpa() != null && student.getCgpa().compareTo(min) >= 0);
    }

    private boolean backlogsOk(JobEligibility rules, Student student) {
        Integer maxActive = rules.getMaxActiveBacklogs();
        Integer maxTotal = rules.getMaxTotalBacklogs();
        if (maxActive != null && student.getActiveBacklogs() > maxActive) {
            return false;
        }
        return maxTotal == null || student.getTotalBacklogs() <= maxTotal;
    }

    private boolean workAuthOk(JobEligibility rules, Student student) {
        WorkAuthorization required = rules.getRequiredWorkAuthorization();
        return required == null
                || required == WorkAuthorization.ANY
                || required == student.getWorkAuthorization();
    }

    private boolean departmentOk(JobEligibility rules, Student student) {
        return containsIgnoreCase(rules.getDepartments(), student.getDepartment());
    }

    private boolean branchOk(JobEligibility rules, Student student) {
        return containsIgnoreCase(rules.getBranches(), student.getBranch());
    }

    private boolean passingYearOk(JobEligibility rules, Student student) {
        Set<Integer> years = rules.getPassingYears();
        return isEmpty(years) || years.contains(student.getPassingYear());
    }

    private boolean locationOk(JobEligibility rules, Student student) {
        return containsIgnoreCase(rules.getAllowedLocations(), student.getLocation());
    }

    private boolean genderOk(JobEligibility rules, Student student) {
        Set<Gender> allowed = rules.getAllowedGenders();
        return isEmpty(allowed) || allowed.contains(student.getGender());
    }

    private boolean ageOk(JobEligibility rules, Student student, LocalDate today) {
        Integer minAge = rules.getMinAge();
        Integer maxAge = rules.getMaxAge();
        if (minAge == null && maxAge == null) {
            return true;
        }
        LocalDate dob = student.getDateOfBirth();
        if (dob == null) {
            // Age is required by the rule but unknown for this student → cannot confirm eligibility.
            return false;
        }
        int age = Period.between(dob, today).getYears();
        if (minAge != null && age < minAge) {
            return false;
        }
        return maxAge == null || age <= maxAge;
    }

    private boolean skillsOk(JobEligibility rules, Student student) {
        Set<String> required = rules.getRequiredSkills();
        if (isEmpty(required)) {
            return true;
        }
        Set<String> studentSkills = normalise(student.getSkills());
        Set<String> requiredNorm = normalise(required);
        SkillMatchMode mode = rules.getSkillMatchMode() == null ? SkillMatchMode.ANY : rules.getSkillMatchMode();
        return switch (mode) {
            case ALL -> studentSkills.containsAll(requiredNorm);
            case ANY -> requiredNorm.stream().anyMatch(studentSkills::contains);
        };
    }

    /**
     * Package-band check: compares the job's advertised salary range against the rule's
     * min/max package expectations. A {@code minPackage} rule requires the job's upper salary
     * to reach it; a {@code maxPackage} rule requires the job's lower salary to stay within it.
     * When the relevant salary figure is absent, the constraint cannot be violated and passes.
     */
    private boolean packageOk(JobEligibility rules, Job job) {
        BigDecimal minPackage = rules.getMinPackage();
        BigDecimal maxPackage = rules.getMaxPackage();
        if (minPackage != null && job.getSalaryMax() != null
                && job.getSalaryMax().compareTo(minPackage) < 0) {
            return false;
        }
        return maxPackage == null || job.getSalaryMin() == null
                || job.getSalaryMin().compareTo(maxPackage) <= 0;
    }

    // ---------------------------------------------------------------- helpers

    private static boolean containsIgnoreCase(Set<String> allowed, String value) {
        if (isEmpty(allowed)) {
            return true;
        }
        if (value == null) {
            return false;
        }
        String needle = value.trim().toLowerCase(Locale.ROOT);
        return allowed.stream()
                .filter(a -> a != null)
                .map(a -> a.trim().toLowerCase(Locale.ROOT))
                .anyMatch(needle::equals);
    }

    private static Set<String> normalise(Set<String> values) {
        if (values == null) {
            return Set.of();
        }
        return values.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(v -> v.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private static boolean isEmpty(Set<?> set) {
        return set == null || set.isEmpty();
    }
}
