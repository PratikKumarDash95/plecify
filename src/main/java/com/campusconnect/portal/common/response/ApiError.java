package com.campusconnect.portal.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

/** Structured error detail carried inside {@link ApiResponse#getError()}. */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    /** Stable, machine-readable code (e.g. {@code RESOURCE_NOT_FOUND}). */
    private final String code;

    private final int status;

    /** Field-level validation failures, when applicable. */
    @Singular
    private final List<FieldViolation> violations;

    @Getter
    @Builder
    public static class FieldViolation {
        private final String field;
        private final String message;
        private final Object rejectedValue;
    }
}
