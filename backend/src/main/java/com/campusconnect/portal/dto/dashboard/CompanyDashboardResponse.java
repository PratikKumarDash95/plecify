package com.campusconnect.portal.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/** Aggregate metrics for a company's hiring dashboard. */
@Schema(description = "Company hiring dashboard metrics")
public record CompanyDashboardResponse(
        long totalJobs,
        long pendingJobs,
        long approvedJobs,
        long rejectedJobs,
        long closedJobs,
        long totalApplications,
        @Schema(description = "Application counts keyed by ApplicationStatus name.")
        Map<String, Long> applicationsByStatus
) {
}
