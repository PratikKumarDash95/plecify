package com.campusconnect.portal.dto.job;

import com.campusconnect.portal.common.enums.Gender;
import com.campusconnect.portal.common.enums.WorkAuthorization;
import com.campusconnect.portal.entity.JobEligibility.SkillMatchMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Declarative eligibility rules for a job, exchanged on create/update and echoed back on
 * read. Every field is optional; a null scalar or empty collection means "no constraint on
 * this dimension". The deterministic engine consumes exactly these rules.
 */
@Schema(description = "Eligibility rules evaluated deterministically by the engine. Null/empty = no constraint.")
public record JobEligibilityDto(

        @Schema(description = "Minimum CGPA on a 0-10 scale (inclusive).", example = "7.50")
        @DecimalMin(value = "0.0") @DecimalMax(value = "10.0") @Digits(integer = 2, fraction = 2)
        BigDecimal minCgpa,

        @Schema(description = "Maximum active backlogs allowed (inclusive).", example = "0")
        @Min(0) @Max(100)
        Integer maxActiveBacklogs,

        @Schema(description = "Maximum total (historic) backlogs allowed (inclusive).", example = "2")
        @Min(0) @Max(100)
        Integer maxTotalBacklogs,

        @Schema(description = "Required work authorization; ANY = no restriction.", example = "ANY")
        WorkAuthorization requiredWorkAuthorization,

        @Schema(description = "How required skills are matched: ALL (every skill) or ANY (at least one).",
                example = "ANY")
        SkillMatchMode skillMatchMode,

        @Schema(description = "Eligible departments. Empty = all departments.")
        Set<String> departments,

        @Schema(description = "Eligible branches. Empty = all branches.")
        Set<String> branches,

        @Schema(description = "Eligible graduation/passing years. Empty = all years.", example = "[2025, 2026]")
        Set<Integer> passingYears,

        @Schema(description = "Required skills (case-insensitive). Empty = no skill requirement.")
        Set<String> requiredSkills,

        @Schema(description = "Eligible student home locations. Empty = any location.")
        Set<String> allowedLocations,

        @Schema(description = "Eligible genders. Empty = no gender constraint.")
        Set<Gender> allowedGenders,

        @Schema(description = "Eligible batch labels. Empty = no batch constraint.", example = "[\"2025\"]")
        Set<String> batches,

        @Schema(description = "Minimum age in years (inclusive).", example = "18")
        @Min(0) @Max(150)
        Integer minAge,

        @Schema(description = "Maximum age in years (inclusive).", example = "27")
        @Min(0) @Max(150)
        Integer maxAge,

        @Schema(description = "Minimum package/CTC the offer must meet (compared to job salaryMax).",
                example = "600000.00")
        @DecimalMin(value = "0.0") @Digits(integer = 12, fraction = 2)
        BigDecimal minPackage,

        @Schema(description = "Maximum package/CTC allowed (compared to job salaryMin).",
                example = "3000000.00")
        @DecimalMin(value = "0.0") @Digits(integer = 12, fraction = 2)
        BigDecimal maxPackage
) {
}
