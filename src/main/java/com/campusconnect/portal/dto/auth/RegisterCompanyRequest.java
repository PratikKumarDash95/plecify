package com.campusconnect.portal.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Self-registration payload for a company (recruiter) account. The account is created
 * disabled and its company profile starts in {@code PENDING} admin approval.
 */
public record RegisterCompanyRequest(
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 150) String contactPersonName,
        @Size(max = 20) String phone,
        @NotBlank @Size(max = 200) String companyName,
        @Size(max = 120) String industry,
        @Size(max = 255) String website,
        @Size(max = 5000) String description,
        @Size(max = 200) String headquarters,
        @Email @Size(max = 180) String contactEmail,
        @Size(max = 20) String contactPhone
) {
}
