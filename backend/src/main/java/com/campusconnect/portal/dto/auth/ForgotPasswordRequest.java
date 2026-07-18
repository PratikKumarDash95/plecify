package com.campusconnect.portal.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Requests a password-reset email for the given account. */
public record ForgotPasswordRequest(
        @NotBlank @Email @Size(max = 180) String email
) {
}
