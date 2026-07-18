package com.campusconnect.portal.exception;

/**
 * Thrown when a domain rule is violated (illegal status transition, ineligible application,
 * duplicate application, etc.). The specific {@link ErrorCode} determines the HTTP status.
 */
public class BusinessRuleException extends ApiException {

    public BusinessRuleException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessRuleException(ErrorCode errorCode) {
        super(errorCode);
    }
}
