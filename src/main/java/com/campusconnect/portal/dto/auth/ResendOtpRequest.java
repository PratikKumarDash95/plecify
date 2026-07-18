package com.campusconnect.portal.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Requests a fresh login OTP during an in-progress two-step login. */
public record ResendOtpRequest(
        @NotBlank @Email @Size(max = 180) String email
) {
}
