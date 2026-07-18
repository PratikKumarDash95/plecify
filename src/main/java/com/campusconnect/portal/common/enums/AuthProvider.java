package com.campusconnect.portal.common.enums;

/**
 * How a user authenticates. {@code LOCAL} accounts have a password hash; federated accounts
 * (e.g. {@code GOOGLE}) are verified by the external identity provider and may have no password.
 */
public enum AuthProvider {
    LOCAL,
    GOOGLE
}
