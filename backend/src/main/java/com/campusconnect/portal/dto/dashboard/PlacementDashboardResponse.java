package com.campusconnect.portal.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;

/** Aggregate metrics for a placement cell's oversight dashboard. */
@Schema(description = "Placement cell dashboard metrics")
public record PlacementDashboardResponse(
        long pendingJobs,
        @Schema(description = "Jobs approved by this cell since local midnight (UTC).")
        long approvedToday,
        @Schema(description = "Jobs rejected by this cell since local midnight (UTC).")
        long rejectedToday,
        long approvedJobs,
        long rejectedJobs,
        long totalStudents,
        long placementEligibleStudents,
        long totalApplications
) {
}
