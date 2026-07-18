package com.campusconnect.portal.dto.job;

import com.campusconnect.portal.common.enums.JobStatus;
import com.campusconnect.portal.common.enums.JobType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Full job detail, including eligibility rules and review metadata. */
@Schema(description = "Full job detail with eligibility rules and review status")
public record JobResponse(
        UUID id,
        UUID companyId,
        String companyName,
        String companyLogoUrl,
        UUID placementCellId,
        UUID universityId,
        String universityName,
        String title,
        String description,
        JobType jobType,
        String location,
        boolean remoteAllowed,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String currency,
        int openings,
        JobStatus status,
        Instant applicationDeadline,
        String rejectionReason,
        Instant reviewedAt,
        UUID approvedBy,
        Instant approvedAt,
        Instant eligibilityComputedAt,
        long eligibleStudentCount,
        long applicationCount,
        JobEligibilityDto eligibility,
        Instant createdAt,
        Instant updatedAt
) {
}
