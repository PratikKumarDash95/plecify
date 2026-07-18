package com.campusconnect.portal.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Requests a fresh email-verification link for an unverified account. */
public record ResendVerificationRequest(
        @NotBlank @Email @Size(max = 180) String email
) {
}
