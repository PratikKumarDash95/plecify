package com.campusconnect.portal.dto.dashboard;

import com.campusconnect.portal.dto.student.EligibleJobResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Aggregate metrics and upcoming-deadline widget for a student's dashboard. */
@Schema(description = "Student dashboard metrics and upcoming deadlines")
public record StudentDashboardResponse(
        boolean placementEligible,
        long eligibleJobs,
        long appliedJobs,
        @Schema(description = "Applications currently at the INTERVIEW_SCHEDULED stage.")
        long interviewCount,
        @Schema(description = "Nearest upcoming application deadlines among eligible jobs.")
        List<EligibleJobResponse> upcomingDeadlines
) {
}
