package com.campusconnect.portal.dto.auth;

import com.campusconnect.portal.common.enums.WorkAuthorization;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;

/** Self-registration payload for a student account. */
public record RegisterStudentRequest(
        @NotBlank @Email @Size(max = 180)
        @Pattern(regexp = "(?i).+@.+\\.edu\\.in$", message = "Email must be a valid .edu.in address")
        String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 150) String fullName,
        @Size(max = 20) String phone,
        @NotBlank @Size(max = 120)
        @Pattern(regexp = "(?i)^[a-z0-9.-]+\\.edu\\.in$", message = "University domain must end in .edu.in")
        String universityDomain,
        @NotBlank @Size(max = 40) String rollNumber,
        @NotBlank @Size(max = 100) String department,
        @NotBlank @Size(max = 100) String branch,
        @Size(max = 60) String degree,
        @NotNull @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal cgpa,
        @PositiveOrZero int activeBacklogs,
        @PositiveOrZero int totalBacklogs,
        @Min(2000) int passingYear,
        WorkAuthorization workAuthorization,
        @Size(max = 120) String location,
        Set<@Size(max = 80) String> skills
) {
}
