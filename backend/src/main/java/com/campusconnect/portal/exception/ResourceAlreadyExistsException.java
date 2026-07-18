package com.campusconnect.portal.exception;

/** Thrown on unique-constraint conflicts (e.g. duplicate email). Maps to HTTP 409. */
public class ResourceAlreadyExistsException extends ApiException {

    public ResourceAlreadyExistsException(String message) {
        super(ErrorCode.RESOURCE_ALREADY_EXISTS, message);
    }

    public ResourceAlreadyExistsException(String resource, String field, Object value) {
        super(ErrorCode.RESOURCE_ALREADY_EXISTS,
                "%s already exists with %s '%s'".formatted(resource, field, value));
    }
}
