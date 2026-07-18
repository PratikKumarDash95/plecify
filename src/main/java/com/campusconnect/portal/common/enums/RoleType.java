package com.campusconnect.portal.common.enums;

/**
 * System roles. Persisted in the {@code roles} table and referenced by Spring Security
 * authorities as {@code ROLE_<name>}.
 */
public enum RoleType {
    ADMIN,
    PLACEMENT_CELL,
    COMPANY,
    STUDENT
}
