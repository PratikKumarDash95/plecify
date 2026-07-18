package com.campusconnect.portal.dto.placement;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/** Outcome of an approval: the job plus how many students the engine matched. */
@Schema(description = "Result of approving a job, including eligibility engine output")
public record ApproveJobResponse(
        UUID jobId,
        String status,
        UUID approvedBy,
        Instant approvedAt,
        @Schema(description = "Number of eligible_jobs rows the engine created for this job.")
        int eligibleStudentsMatched,
        @Schema(description = "Number of new-eligible-job notifications dispatched.")
        int notificationsDispatched
) {
}
