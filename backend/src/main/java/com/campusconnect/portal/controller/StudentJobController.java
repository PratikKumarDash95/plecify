package com.campusconnect.portal.controller;

import com.campusconnect.portal.common.response.ApiResponse;
import com.campusconnect.portal.common.response.PagedResponse;
import com.campusconnect.portal.dto.dashboard.StudentDashboardResponse;
import com.campusconnect.portal.dto.student.ApplicationResponse;
import com.campusconnect.portal.dto.student.ApplyJobRequest;
import com.campusconnect.portal.dto.student.EligibleJobResponse;
import com.campusconnect.portal.security.AuthenticatedUser;
import com.campusconnect.portal.security.CurrentUser;
import com.campusconnect.portal.service.student.StudentJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Student-facing discovery and application endpoints. Restricted to the {@code STUDENT} role.
 * Students only ever see jobs through the pre-computed {@code eligible_jobs} table.
 */
@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@Tag(name = "Student Jobs", description = "Eligible jobs, applications, and dashboard for students")
public class StudentJobController {

    private final StudentJobService studentJobService;

    @Operation(summary = "List eligible jobs",
            description = "Paginated list of the student's pre-computed, not-yet-applied eligible jobs.")
    @GetMapping("/eligible-jobs")
    public ApiResponse<PagedResponse<EligibleJobResponse>> listEligibleJobs(
            @CurrentUser AuthenticatedUser user,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<EligibleJobResponse> page = studentJobService.listEligibleJobs(user.id(), pageable);
        return ApiResponse.success(PagedResponse.of(page));
    }

    @Operation(summary = "Apply to a job",
            description = "Applies to an eligible job. Fails if the student is not eligible, has already "
                    + "applied, or the job is no longer open.")
    @PostMapping("/jobs/{jobId}/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ApplicationResponse> apply(@CurrentUser AuthenticatedUser user,
                                                  @PathVariable UUID jobId,
                                                  @Valid @RequestBody ApplyJobRequest request) {
        ApplicationResponse response = studentJobService.applyToJob(user.id(), jobId, request);
        return ApiResponse.success(response, "Application submitted");
    }

    @Operation(summary = "List my applications",
            description = "Paginated list of the student's applications across all jobs.")
    @GetMapping("/applications")
    public ApiResponse<PagedResponse<ApplicationResponse>> listApplications(
            @CurrentUser AuthenticatedUser user,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ApplicationResponse> page = studentJobService.listApplications(user.id(), pageable);
        return ApiResponse.success(PagedResponse.of(page));
    }

    @Operation(summary = "Student dashboard",
            description = "Aggregate metrics (eligible/applied counts) plus the nearest upcoming deadlines.")
    @GetMapping("/dashboard")
    public ApiResponse<StudentDashboardResponse> dashboard(@CurrentUser AuthenticatedUser user) {
        return ApiResponse.success(studentJobService.getDashboard(user.id()));
    }
}
