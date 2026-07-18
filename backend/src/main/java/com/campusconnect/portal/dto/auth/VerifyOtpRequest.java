package com.campusconnect.portal.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Completes a two-step login: the email from the challenge plus the emailed 6-digit code. */
public record VerifyOtpRequest(
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "Code must be 6 digits") String code
) {
}
