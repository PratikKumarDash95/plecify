package com.campusconnect.portal.controller;

import com.campusconnect.portal.common.response.ApiResponse;
import com.campusconnect.portal.common.response.PagedResponse;
import com.campusconnect.portal.dto.job.JobResponse;
import com.campusconnect.portal.dto.job.JobSummaryResponse;
import com.campusconnect.portal.dto.dashboard.PlacementDashboardResponse;
import com.campusconnect.portal.dto.placement.ApproveJobResponse;
import com.campusconnect.portal.dto.placement.RejectJobRequest;
import com.campusconnect.portal.security.AuthenticatedUser;
import com.campusconnect.portal.security.CurrentUser;
import com.campusconnect.portal.service.placement.PlacementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Placement-cell review endpoints. Restricted to the {@code PLACEMENT_CELL} role; the service
 * layer further scopes every action to jobs targeting the cell's own university. Approving a
 * job triggers the deterministic eligibility engine.
 */
@RestController
@RequestMapping("/api/v1/placement/jobs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PLACEMENT_CELL')")
@Tag(name = "Placement Review", description = "Review, approve, and reject company job postings")
public class PlacementController {

    private final PlacementService placementService;

    @Operation(summary = "List pending jobs",
            description = "Paginated list of jobs awaiting this placement cell's review.")
    @GetMapping("/pending")
    public ApiResponse<PagedResponse<JobSummaryResponse>> listPending(
            @CurrentUser AuthenticatedUser user,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<JobSummaryResponse> page = placementService.listPendingJobs(user.id(), pageable);
        return ApiResponse.success(PagedResponse.of(page));
    }

    @Operation(summary = "Get a job for review",
            description = "Full detail for a job assigned to this cell's university.")
    @GetMapping("/{jobId}")
    public ApiResponse<JobResponse> getJob(@CurrentUser AuthenticatedUser user,
                                           @PathVariable UUID jobId) {
        return ApiResponse.success(placementService.getJob(user.id(), jobId));
    }

    @Operation(summary = "Approve a job",
            description = "Approves a pending job, runs the eligibility engine, and makes it visible to "
                    + "matched students. Returns the number of students matched.")
    @PostMapping("/{jobId}/approve")
    public ApiResponse<ApproveJobResponse> approveJob(@CurrentUser AuthenticatedUser user,
                                                     @PathVariable UUID jobId) {
        ApproveJobResponse response = placementService.approveJob(user.id(), jobId);
        return ApiResponse.success(response, "Job approved and eligibility computed");
    }

    @Operation(summary = "Reject a job",
            description = "Rejects a pending job with a mandatory reason. The job never becomes visible "
                    + "to students.")
    @PostMapping("/{jobId}/reject")
    public ApiResponse<JobResponse> rejectJob(@CurrentUser AuthenticatedUser user,
                                              @PathVariable UUID jobId,
                                              @Valid @RequestBody RejectJobRequest request) {
        JobResponse response = placementService.rejectJob(user.id(), jobId, request.reason());
        return ApiResponse.success(response, "Job rejected");
    }

    @Operation(summary = "Placement cell dashboard",
            description = "Aggregate metrics for this cell: pending/approved/rejected jobs, "
                    + "approvals and rejections today, student counts, and total applications.")
    @GetMapping("/dashboard")
    public ApiResponse<PlacementDashboardResponse> dashboard(@CurrentUser AuthenticatedUser user) {
        return ApiResponse.success(placementService.getDashboard(user.id()));
    }
}
