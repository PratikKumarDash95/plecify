package com.campusconnect.portal.dto.student;

import com.campusconnect.portal.common.enums.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/** A student's application to a job, as returned after applying or when listing applications. */
@Schema(description = "A student's job application")
public record ApplicationResponse(
        UUID id,
        UUID jobId,
        String jobTitle,
        UUID companyId,
        String companyName,
        ApplicationStatus status,
        String resumeUrl,
        String coverLetter,
        Instant interviewAt,
        String interviewDetails,
        String statusNote,
        Instant lastStatusChangeAt,
        Instant createdAt
) {
}
