package com.campusconnect.portal.exception;

import org.springframework.http.HttpStatus;

/** Central catalogue of application error codes and their HTTP mapping. */
public enum ErrorCode {

    // 400
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "One or more fields are invalid"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "The request could not be processed"),
    ILLEGAL_STATE_TRANSITION(HttpStatus.BAD_REQUEST, "The requested state transition is not allowed"),
    FILE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST, "File could not be uploaded"),

    // 401
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "Authentication failed"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token has expired"),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Token is invalid"),
    EMAIL_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, "Email address has not been verified"),

    // 403
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "You do not have permission to perform this action"),
    ACCOUNT_NOT_APPROVED(HttpStatus.FORBIDDEN, "Account is pending approval"),

    // 404
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested resource was not found"),

    // 409
    RESOURCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "The resource already exists"),
    DUPLICATE_APPLICATION(HttpStatus.CONFLICT, "You have already applied to this job"),
    NOT_ELIGIBLE(HttpStatus.CONFLICT, "You are not eligible for this job"),

    // 429
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Please try again later"),

    // 500 / 502 / 503
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred"),
    EMAIL_DELIVERY_ERROR(HttpStatus.BAD_GATEWAY, "Failed to deliver email"),
    STORAGE_ERROR(HttpStatus.BAD_GATEWAY, "Storage backend error"),
    AI_SERVICE_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "AI service is unavailable");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
