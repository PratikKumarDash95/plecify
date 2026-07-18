package com.campusconnect.portal.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Uniform envelope for every REST response. Controllers never return raw entities or DTOs;
 * they wrap them here so clients get a predictable shape for both success and error cases.
 *
 * @param <T> the payload type
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final ApiError error;
    private final Instant timestamp;
    private final String path;

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Request processed successfully");
    }

    public static ApiResponse<Void> message(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> failure(String message, ApiError error, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(error)
                .path(path)
                .timestamp(Instant.now())
                .build();
    }
}
