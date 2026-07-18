package com.campusconnect.portal.service.eligibility;

import java.util.UUID;

/**
 * Deterministic eligibility engine. Given an approved job and its declared rules, it
 * evaluates every placement-eligible student of the target university and materialises one
 * {@code eligible_jobs} row per match. Runs are idempotent: re-running for the same job
 * reconciles the table (adds new matches, revokes rows that no longer qualify) without
 * creating duplicates, satisfying the mandatory pre-computation requirement.
 */
public interface EligibilityEngineService {

    /**
     * Computes and materialises eligibility for a single job.
     *
     * @param jobId the job to evaluate (must be loadable with its eligibility rules)
     * @return summary of how many rows were created/revoked and students notified
     */
    EligibilityRunResult computeForJob(UUID jobId);

    /** Immutable outcome of an engine run. */
    record EligibilityRunResult(
            UUID jobId,
            int candidatesEvaluated,
            int matched,
            int newlyMatched,
            int revoked,
            int notificationsDispatched
    ) {
    }
}
