package com.campusconnect.portal.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Optional details supplied when a student applies. If {@code resumeUrl} is omitted the
 * student's current profile resume is snapshotted onto the application.
 */
@Schema(description = "Apply to an eligible job")
public record ApplyJobRequest(
        @Schema(description = "Resume URL to snapshot; defaults to the profile resume when omitted.")
        @Size(max = 512) String resumeUrl,

        @Schema(description = "Optional cover letter.")
        @Size(max = 20000) String coverLetter
) {
}
