package com.campusconnect.portal.service.job;

import com.campusconnect.portal.dto.dashboard.CompanyDashboardResponse;
import com.campusconnect.portal.dto.job.CreateJobRequest;
import com.campusconnect.portal.dto.job.JobResponse;
import com.campusconnect.portal.dto.job.JobSummaryResponse;
import com.campusconnect.portal.dto.job.UpdateJobRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Company-facing job lifecycle operations. Jobs are created in {@code PENDING} status and
 * are editable only while pending; approval/rejection is handled by the placement cell in
 * {@link com.campusconnect.portal.service.placement.PlacementService}.
 */
public interface JobService {

    /**
     * Creates a job for the authenticated company, targeting a university's placement cell.
     *
     * @param companyUserId the authenticated company user's id
     * @param request       the posting details and eligibility rules
     * @return the created job (PENDING)
     */
    JobResponse createJob(UUID companyUserId, CreateJobRequest request);

    /**
     * Updates a still-pending job owned by the authenticated company.
     *
     * @throws com.campusconnect.portal.exception.BusinessRuleException if the job is not PENDING
     */
    JobResponse updateJob(UUID companyUserId, UUID jobId, UpdateJobRequest request);

    /** Returns full detail for a job the authenticated company owns. */
    JobResponse getCompanyJob(UUID companyUserId, UUID jobId);

    /** Lists the authenticated company's jobs (all statuses). */
    Page<JobSummaryResponse> listCompanyJobs(UUID companyUserId, Pageable pageable);

    /**
     * Deletes a still-pending job owned by the authenticated company.
     *
     * @throws com.campusconnect.portal.exception.BusinessRuleException if the job is not PENDING
     */
    void deleteJob(UUID companyUserId, UUID jobId);

    /** Aggregated counters for the authenticated company's dashboard. */
    CompanyDashboardResponse getCompanyDashboard(UUID companyUserId);
}
