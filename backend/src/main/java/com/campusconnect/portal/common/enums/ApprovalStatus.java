package com.campusconnect.portal.common.enums;

/**
 * Onboarding/verification status for tenant entities (companies, placement cells).
 * A COMPANY account can log in but cannot post jobs until {@link #APPROVED} by an admin.
 */
public enum ApprovalStatus {
    PENDING,
    APPROVED,
    SUSPENDED,
    REJECTED
}
