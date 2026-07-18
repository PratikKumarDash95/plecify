package com.campusconnect.portal.dto.job;

import com.campusconnect.portal.common.enums.JobStatus;
import com.campusconnect.portal.common.enums.JobType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Lightweight job card for list/table views. Excludes the full description and rules. */
@Schema(description = "Compact job summary for listings")
public record JobSummaryResponse(
        UUID id,
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
        JobStatus status,
        Instant applicationDeadline,
        Instant createdAt
) {
}
