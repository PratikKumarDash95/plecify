package com.campusconnect.portal.dto.job;

import com.campusconnect.portal.common.enums.JobType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payload for editing a job. Only permitted while the job is still {@code PENDING} review;
 * approved jobs are immutable. The target campus/placement cell cannot be changed after
 * creation, so {@code universityId} is intentionally absent here.
 */
@Schema(description = "Update a PENDING job posting. Approved jobs cannot be edited.")
public record UpdateJobRequest(

        @NotBlank @Size(max = 200) String title,

        @NotBlank @Size(max = 20000) String description,

        @NotNull JobType jobType,

        @Size(max = 150) String location,

        boolean remoteAllowed,

        @DecimalMin(value = "0.0") @Digits(integer = 12, fraction = 2) BigDecimal salaryMin,

        @DecimalMin(value = "0.0") @Digits(integer = 12, fraction = 2) BigDecimal salaryMax,

        @Size(min = 3, max = 3) String currency,

        @Min(1) @Max(100000) Integer openings,

        @NotNull @Future Instant applicationDeadline,

        @Schema(description = "Replacement eligibility rules. Omit to clear all constraints.")
        @Valid JobEligibilityDto eligibility
) {
}
