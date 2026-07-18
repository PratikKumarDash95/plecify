package com.campusconnect.portal.common.enums;

/**
 * Student gender, and (via {@code JobEligibility.allowedGenders}) an optional job-side
 * eligibility constraint. {@link #UNDISCLOSED} students are treated as "no value" and are
 * excluded only when a job explicitly restricts by gender.
 */
public enum Gender {
    MALE,
    FEMALE,
    OTHER,
    UNDISCLOSED
}
