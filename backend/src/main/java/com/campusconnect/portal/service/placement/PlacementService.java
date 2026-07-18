package com.campusconnect.portal.service.placement;

import com.campusconnect.portal.dto.dashboard.PlacementDashboardResponse;
import com.campusconnect.portal.dto.job.JobResponse;
import com.campusconnect.portal.dto.job.JobSummaryResponse;
import com.campusconnect.portal.dto.placement.ApproveJobResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Placement-cell operations for reviewing company job postings. A cell only ever sees and
 * acts on jobs targeting its own university. Approving a job runs the deterministic
 * eligibility engine and makes the job visible to matched students.
 */
public interface PlacementService {

    /** Lists jobs awaiting this cell's review (PENDING), scoped to the cell's university. */
    Page<JobSummaryResponse> listPendingJobs(UUID placementUserId, Pageable pageable);

    /** Returns full detail for a pending/reviewed job belonging to this cell's university. */
    JobResponse getJob(UUID placementUserId, UUID jobId);

    /**
     * Approves a pending job: flips it to APPROVED, records the reviewer, runs the eligibility
     * engine, and notifies the company.
     *
     * @return approval outcome including the number of students matched
     */
    ApproveJobResponse approveJob(UUID placementUserId, UUID jobId);

    /**
     * Rejects a pending job with a mandatory reason and notifies the company. The job never
     * becomes visible to students.
     */
    JobResponse rejectJob(UUID placementUserId, UUID jobId, String reason);

    /** Aggregated counters for the placement-cell dashboard. */
    PlacementDashboardResponse getDashboard(UUID placementUserId);
}
