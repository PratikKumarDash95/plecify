package com.campusconnect.portal.exception;

/** Thrown when an entity cannot be found by id or unique key. Maps to HTTP 404. */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public ResourceNotFoundException(String resource, Object identifier) {
        super(ErrorCode.RESOURCE_NOT_FOUND, "%s not found with identifier '%s'".formatted(resource, identifier));
    }
}
