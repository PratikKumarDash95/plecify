package com.campusconnect.portal.dto.student;

import com.campusconnect.portal.common.enums.EligibleJobStatus;
import com.campusconnect.portal.common.enums.JobType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A job as it appears on the student dashboard — read straight from the pre-computed
 * {@code eligible_jobs} table. {@code eligibilityStatus} distinguishes open (ELIGIBLE) from
 * already-applied (APPLIED) rows without a join.
 */
@Schema(description = "A pre-computed eligible job for the student dashboard")
public record EligibleJobResponse(
        UUID eligibleJobId,
        EligibleJobStatus eligibilityStatus,
        UUID jobId,
        UUID companyId,
        String companyName,
        String companyLogoUrl,
        String title,
        JobType jobType,
        String location,
        boolean remoteAllowed,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String currency,
        int openings,
        Instant applicationDeadline,
        Instant matchedAt
) {
}
