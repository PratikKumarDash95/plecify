package com.campusconnect.portal.common.enums;

/**
 * Lifecycle of a job posting.
 *
 * <pre>
 *  DRAFT ─▶ PENDING ─▶ APPROVED ─▶ (CLOSED | EXPIRED)
 *                   └▶ REJECTED
 * </pre>
 *
 * Only {@link #APPROVED} jobs are ever exposed to students, and only through the
 * pre-computed {@code eligible_jobs} table. {@link #REJECTED} jobs are never visible.
 */
public enum JobStatus {
    DRAFT,
    PENDING,
    APPROVED,
    REJECTED,
    CLOSED,
    EXPIRED
}
