package com.campusconnect.portal.service.student;

import com.campusconnect.portal.dto.dashboard.StudentDashboardResponse;
import com.campusconnect.portal.dto.student.ApplicationResponse;
import com.campusconnect.portal.dto.student.ApplyJobRequest;
import com.campusconnect.portal.dto.student.EligibleJobResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Student-facing job discovery and application. Students only ever see jobs through the
 * pre-computed {@code eligible_jobs} table — eligibility is never evaluated at read time —
 * and can only apply to a job for which they hold an {@code ELIGIBLE} row.
 */
public interface StudentJobService {

    /** Lists the student's pre-computed eligible (not-yet-applied) jobs. */
    Page<EligibleJobResponse> listEligibleJobs(UUID studentUserId, Pageable pageable);

    /**
     * Applies to a job the student is eligible for. Flips the eligibility row to APPLIED,
     * creates the application, seeds its status history, and notifies the company.
     *
     * @throws com.campusconnect.portal.exception.BusinessRuleException if not eligible,
     *         already applied, or the job is no longer open
     */
    ApplicationResponse applyToJob(UUID studentUserId, UUID jobId, ApplyJobRequest request);

    /** Lists the student's applications across all jobs. */
    Page<ApplicationResponse> listApplications(UUID studentUserId, Pageable pageable);

    /** Aggregate dashboard metrics plus the nearest upcoming deadlines. */
    StudentDashboardResponse getDashboard(UUID studentUserId);
}
