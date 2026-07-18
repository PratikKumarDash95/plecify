package com.campusconnect.portal.mapper;

import com.campusconnect.portal.dto.job.CreateJobRequest;
import com.campusconnect.portal.dto.job.JobEligibilityDto;
import com.campusconnect.portal.dto.job.JobResponse;
import com.campusconnect.portal.dto.job.JobSummaryResponse;
import com.campusconnect.portal.dto.student.EligibleJobResponse;
import com.campusconnect.portal.entity.EligibleJob;
import com.campusconnect.portal.entity.Job;
import com.campusconnect.portal.entity.JobEligibility;
import com.campusconnect.portal.entity.JobEligibility.SkillMatchMode;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Converts between job entities and their API projections. Kept as a hand-written component
 * (rather than MapStruct) because it manages the {@link JobEligibility} child aggregate and
 * folds in per-job counts computed by the service layer.
 */
@Component
public class JobMapper {

    /** Builds a new {@link Job} (sans company/cell, set by the service) from a create request. */
    public Job toEntity(CreateJobRequest request) {
        Job job = Job.builder()
                .title(request.title())
                .description(request.description())
                .jobType(request.jobType())
                .location(request.location())
                .remoteAllowed(request.remoteAllowed())
                .salaryMin(request.salaryMin())
                .salaryMax(request.salaryMax())
                .currency(request.currency() != null ? request.currency() : "USD")
                .openings(request.openings() != null ? request.openings() : 1)
                .applicationDeadline(request.applicationDeadline())
                .build();
        job.setEligibility(toEligibilityEntity(request.eligibility()));
        return job;
    }

    /** Maps eligibility rules; a null DTO yields an all-open (no-constraint) rule set. */
    public JobEligibility toEligibilityEntity(JobEligibilityDto dto) {
        JobEligibility.JobEligibilityBuilder builder = JobEligibility.builder();
        if (dto == null) {
            return builder.build();
        }
        return builder
                .minCgpa(dto.minCgpa())
                .maxActiveBacklogs(dto.maxActiveBacklogs())
                .maxTotalBacklogs(dto.maxTotalBacklogs())
                .requiredWorkAuthorization(dto.requiredWorkAuthorization() != null
                        ? dto.requiredWorkAuthorization()
                        : com.campusconnect.portal.common.enums.WorkAuthorization.ANY)
                .skillMatchMode(dto.skillMatchMode() != null ? dto.skillMatchMode() : SkillMatchMode.ANY)
                .departments(copy(dto.departments()))
                .branches(copy(dto.branches()))
                .passingYears(copy(dto.passingYears()))
                .requiredSkills(copy(dto.requiredSkills()))
                .allowedLocations(copy(dto.allowedLocations()))
                .allowedGenders(copy(dto.allowedGenders()))
                .batches(copy(dto.batches()))
                .minAge(dto.minAge())
                .maxAge(dto.maxAge())
                .minPackage(dto.minPackage())
                .maxPackage(dto.maxPackage())
                .build();
    }

    /**
     * Overwrites an existing eligibility row in place from a DTO (used on update, preserving
     * the row id and FK so re-runs stay idempotent). A null DTO clears every constraint.
     */
    public void applyEligibility(JobEligibility target, JobEligibilityDto dto) {
        if (dto == null) {
            target.setMinCgpa(null);
            target.setMaxActiveBacklogs(null);
            target.setMaxTotalBacklogs(null);
            target.setRequiredWorkAuthorization(com.campusconnect.portal.common.enums.WorkAuthorization.ANY);
            target.setSkillMatchMode(SkillMatchMode.ANY);
            replace(target.getDepartments(), null);
            replace(target.getBranches(), null);
            replace(target.getPassingYears(), null);
            replace(target.getRequiredSkills(), null);
            replace(target.getAllowedLocations(), null);
            replace(target.getAllowedGenders(), null);
            replace(target.getBatches(), null);
            target.setMinAge(null);
            target.setMaxAge(null);
            target.setMinPackage(null);
            target.setMaxPackage(null);
            return;
        }
        target.setMinCgpa(dto.minCgpa());
        target.setMaxActiveBacklogs(dto.maxActiveBacklogs());
        target.setMaxTotalBacklogs(dto.maxTotalBacklogs());
        target.setRequiredWorkAuthorization(dto.requiredWorkAuthorization() != null
                ? dto.requiredWorkAuthorization()
                : com.campusconnect.portal.common.enums.WorkAuthorization.ANY);
        target.setSkillMatchMode(dto.skillMatchMode() != null ? dto.skillMatchMode() : SkillMatchMode.ANY);
        replace(target.getDepartments(), dto.departments());
        replace(target.getBranches(), dto.branches());
        replace(target.getPassingYears(), dto.passingYears());
        replace(target.getRequiredSkills(), dto.requiredSkills());
        replace(target.getAllowedLocations(), dto.allowedLocations());
        replace(target.getAllowedGenders(), dto.allowedGenders());
        replace(target.getBatches(), dto.batches());
        target.setMinAge(dto.minAge());
        target.setMaxAge(dto.maxAge());
        target.setMinPackage(dto.minPackage());
        target.setMaxPackage(dto.maxPackage());
    }

    public JobEligibilityDto toEligibilityDto(JobEligibility e) {
        if (e == null) {
            return null;
        }
        return new JobEligibilityDto(
                e.getMinCgpa(),
                e.getMaxActiveBacklogs(),
                e.getMaxTotalBacklogs(),
                e.getRequiredWorkAuthorization(),
                e.getSkillMatchMode(),
                copy(e.getDepartments()),
                copy(e.getBranches()),
                copy(e.getPassingYears()),
                copy(e.getRequiredSkills()),
                copy(e.getAllowedLocations()),
                copy(e.getAllowedGenders()),
                copy(e.getBatches()),
                e.getMinAge(),
                e.getMaxAge(),
                e.getMinPackage(),
                e.getMaxPackage());
    }

    public JobResponse toResponse(Job job, long eligibleStudentCount, long applicationCount) {
        var company = job.getCompany();
        var cell = job.getPlacementCell();
        return new JobResponse(
                job.getId(),
                company.getId(),
                company.getName(),
                company.getLogoUrl(),
                cell.getId(),
                cell.getUniversity().getId(),
                cell.getUniversity().getName(),
                job.getTitle(),
                job.getDescription(),
                job.getJobType(),
                job.getLocation(),
                job.isRemoteAllowed(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getCurrency(),
                job.getOpenings(),
                job.getStatus(),
                job.getApplicationDeadline(),
                job.getRejectionReason(),
                job.getReviewedAt(),
                job.getApprovedBy(),
                job.getApprovedAt(),
                job.getEligibilityComputedAt(),
                eligibleStudentCount,
                applicationCount,
                toEligibilityDto(job.getEligibility()),
                job.getCreatedAt(),
                job.getUpdatedAt());
    }

    public JobSummaryResponse toSummary(Job job) {
        var company = job.getCompany();
        return new JobSummaryResponse(
                job.getId(),
                company.getId(),
                company.getName(),
                company.getLogoUrl(),
                job.getTitle(),
                job.getJobType(),
                job.getLocation(),
                job.isRemoteAllowed(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getCurrency(),
                job.getOpenings(),
                job.getStatus(),
                job.getApplicationDeadline(),
                job.getCreatedAt());
    }

    /** Maps a materialised {@link EligibleJob} row to the student-facing card. */
    public EligibleJobResponse toEligibleJobResponse(EligibleJob ej) {
        Job job = ej.getJob();
        var company = job.getCompany();
        return new EligibleJobResponse(
                ej.getId(),
                ej.getStatus(),
                job.getId(),
                company.getId(),
                company.getName(),
                company.getLogoUrl(),
                job.getTitle(),
                job.getJobType(),
                job.getLocation(),
                job.isRemoteAllowed(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getCurrency(),
                job.getOpenings(),
                job.getApplicationDeadline(),
                ej.getCreatedAt());
    }

    private static <T> Set<T> copy(Set<T> source) {
        return source == null ? new HashSet<>() : new HashSet<>(source);
    }

    /** Mutates a managed collection in place (clear + addAll) to keep Hibernate happy on update. */
    private static <T> void replace(Set<T> managed, Set<T> incoming) {
        managed.clear();
        if (incoming != null) {
            managed.addAll(incoming);
        }
    }
}
