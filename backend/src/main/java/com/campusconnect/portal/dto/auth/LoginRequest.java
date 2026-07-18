package com.campusconnect.portal.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Credentials payload for the login endpoint. */
public record LoginRequest(
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(max = 72) String password
) {
}
