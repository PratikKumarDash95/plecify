package com.campusconnect.portal.common.enums;

/**
 * Status of a pre-computed eligibility record in {@code eligible_jobs}.
 *
 * <p>{@link #ELIGIBLE} rows are what a student sees on their dashboard. When a student
 * applies, the row moves to {@link #APPLIED} so the dashboard can separate open vs applied
 * jobs without a join. If a job is closed or a student's profile changes, the row can be
 * marked {@link #REVOKED} rather than deleted, preserving history.
 */
public enum EligibleJobStatus {
    ELIGIBLE,
    APPLIED,
    REVOKED
}
