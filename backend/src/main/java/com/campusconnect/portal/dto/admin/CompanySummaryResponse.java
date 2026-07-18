package com.campusconnect.portal.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/** Row in the admin company-review list. */
@Schema(description = "Company awaiting or having completed admin review")
public record CompanySummaryResponse(
        UUID id,
        String name,
        String industry,
        String contactPersonName,
        String contactEmail,
        String status,
        Instant registeredAt
) {
}
