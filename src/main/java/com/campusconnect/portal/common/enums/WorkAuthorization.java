package com.campusconnect.portal.common.enums;

/**
 * Work authorization status of a student, and the requirement declared on a job.
 * A job may require {@link #ANY} (no restriction) or a specific authorization.
 */
public enum WorkAuthorization {
    CITIZEN,
    PERMANENT_RESIDENT,
    REQUIRES_SPONSORSHIP,
    ANY
}
