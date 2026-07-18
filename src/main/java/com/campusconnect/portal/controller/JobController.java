package com.campusconnect.portal.controller;

import com.campusconnect.portal.common.response.ApiResponse;
import com.campusconnect.portal.common.response.PagedResponse;
import com.campusconnect.portal.dto.dashboard.CompanyDashboardResponse;
import com.campusconnect.portal.dto.job.CreateJobRequest;
import com.campusconnect.portal.dto.job.JobResponse;
import com.campusconnect.portal.dto.job.JobSummaryResponse;
import com.campusconnect.portal.dto.job.UpdateJobRequest;
import com.campusconnect.portal.security.AuthenticatedUser;
import com.campusconnect.portal.security.CurrentUser;
import com.campusconnect.portal.service.job.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Company job-management endpoints. Every route is restricted to the {@code COMPANY} role and
 * scoped to the authenticated company's own postings by the service layer.
 */
@RestController
@RequestMapping("/api/v1/company/jobs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COMPANY')")
@Tag(name = "Company Jobs", description = "Create, edit, and view a company's own job postings")
public class JobController {

    private final JobService jobService;

    @Operation(summary = "Create a job posting",
            description = "Creates a job in PENDING status targeting a university's placement cell for review.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<JobResponse> createJob(@CurrentUser AuthenticatedUser user,
                                              @Valid @RequestBody CreateJobRequest request) {
        JobResponse response = jobService.createJob(user.id(), request);
        return ApiResponse.success(response, "Job created and submitted for review");
    }

    @Operation(summary = "Update a pending job",
            description = "Edits a job that is still PENDING review. Approved or rejected jobs are immutable.")
    @PutMapping("/{jobId}")
    public ApiResponse<JobResponse> updateJob(@CurrentUser AuthenticatedUser user,
                                              @PathVariable UUID jobId,
                                              @Valid @RequestBody UpdateJobRequest request) {
        JobResponse response = jobService.updateJob(user.id(), jobId, request);
        return ApiResponse.success(response, "Job updated");
    }

    @Operation(summary = "Get one of the company's jobs",
            description = "Returns full detail, including eligibility rules and match/application counts.")
    @GetMapping("/{jobId}")
    public ApiResponse<JobResponse> getJob(@CurrentUser AuthenticatedUser user,
                                           @PathVariable UUID jobId) {
        return ApiResponse.success(jobService.getCompanyJob(user.id(), jobId));
    }

    @Operation(summary = "List the company's jobs",
            description = "Paginated list of the authenticated company's postings across all statuses.")
    @GetMapping
    public ApiResponse<PagedResponse<JobSummaryResponse>> listJobs(
            @CurrentUser AuthenticatedUser user,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<JobSummaryResponse> page = jobService.listCompanyJobs(user.id(), pageable);
        return ApiResponse.success(PagedResponse.of(page));
    }

    @Operation(summary = "Delete a pending job",
            description = "Deletes a job that is still PENDING review. Approved or rejected jobs cannot be deleted.")
    @DeleteMapping("/{jobId}")
    public ApiResponse<Void> deleteJob(@CurrentUser AuthenticatedUser user,
                                       @PathVariable UUID jobId) {
        jobService.deleteJob(user.id(), jobId);
        return ApiResponse.message("Job deleted");
    }

    @Operation(summary = "Company dashboard",
            description = "Aggregate metrics: job counts by status and applications by stage.")
    @GetMapping("/dashboard")
    public ApiResponse<CompanyDashboardResponse> dashboard(@CurrentUser AuthenticatedUser user) {
        return ApiResponse.success(jobService.getCompanyDashboard(user.id()));
    }
}
