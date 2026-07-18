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
import java.util.UUID;

/**
 * Payload for creating a job posting. The company is derived from the authenticated
 * principal; the target campus's placement cell is resolved from {@code universityId}. The
 * job is created in {@code PENDING} status and is invisible to students until approved.
 */
@Schema(description = "Create a job posting (enters PENDING review).")
public record CreateJobRequest(

        @Schema(description = "University whose placement cell will review this posting.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID universityId,

        @Schema(example = "Software Engineer, New Grad", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 200) String title,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 20000) String description,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "FULL_TIME")
        @NotNull JobType jobType,

        @Schema(example = "Bengaluru, IN")
        @Size(max = 150) String location,

        @Schema(description = "Whether remote work is allowed.", example = "false")
        boolean remoteAllowed,

        @Schema(example = "800000.00")
        @DecimalMin(value = "0.0") @Digits(integer = 12, fraction = 2) BigDecimal salaryMin,

        @Schema(example = "1500000.00")
        @DecimalMin(value = "0.0") @Digits(integer = 12, fraction = 2) BigDecimal salaryMax,

        @Schema(example = "INR")
        @Size(min = 3, max = 3) String currency,

        @Schema(description = "Number of open positions.", example = "5")
        @Min(1) @Max(100000) Integer openings,

        @Schema(description = "Application deadline; must be in the future.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Future Instant applicationDeadline,

        @Schema(description = "Deterministic eligibility rules. Omit for an open-to-all posting.")
        @Valid JobEligibilityDto eligibility
) {
}
