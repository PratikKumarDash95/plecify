package com.campusconnect.portal.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/** Full company detail for the admin review screen. */
@Schema(description = "Full company profile for admin review")
public record CompanyResponse(
        UUID id,
        String name,
        String industry,
        String website,
        String description,
        String logoUrl,
        String headquarters,
        String contactPersonName,
        String contactEmail,
        String contactPhone,
        String accountEmail,
        String status,
        Instant registeredAt
) {
}
