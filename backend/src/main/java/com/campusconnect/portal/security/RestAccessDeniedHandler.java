package com.campusconnect.portal.security;

import com.campusconnect.portal.common.response.ApiError;
import com.campusconnect.portal.common.response.ApiResponse;
import com.campusconnect.portal.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Returns a JSON {@link ApiResponse} 403 for authenticated-but-unauthorized access. */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ErrorCode code = ErrorCode.ACCESS_DENIED;
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> body = ApiResponse.failure(
                code.getDefaultMessage(),
                ApiError.builder().code(code.name()).status(HttpStatus.FORBIDDEN.value()).build(),
                request.getRequestURI());
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
