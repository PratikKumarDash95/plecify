package com.campusconnect.portal.dto.placement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payload for rejecting a pending job; the reason is surfaced to the company. */
@Schema(description = "Reject a pending job with a mandatory reason")
public record RejectJobRequest(
        @Schema(description = "Reason shown to the company.", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "Salary range does not meet campus minimum for this batch.")
        @NotBlank @Size(max = 500) String reason
) {
}
