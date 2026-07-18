package com.campusconnect.portal.exception;

import lombok.Getter;

/**
 * Base class for all application-thrown exceptions. Carries an {@link ErrorCode} so the
 * global handler can produce a consistent HTTP status and error body.
 */
@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApiException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
