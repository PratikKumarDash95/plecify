package com.campusconnect.portal.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payload for rejecting a company registration; the reason is surfaced to the company. */
@Schema(description = "Reject a company registration with a mandatory reason")
public record RejectCompanyRequest(
        @Schema(description = "Reason shown to the company.", requiredMode = Schema.RequiredMode.REQUIRED,
                example = "Could not verify the company's registration details.")
        @NotBlank @Size(max = 500) String reason
) {
}
