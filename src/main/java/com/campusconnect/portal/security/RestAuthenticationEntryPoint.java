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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Returns a JSON {@link ApiResponse} 401 for unauthenticated access to protected routes. */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorCode code = ErrorCode.AUTHENTICATION_FAILED;
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> body = ApiResponse.failure(
                "Authentication is required to access this resource",
                ApiError.builder().code(code.name()).status(HttpStatus.UNAUTHORIZED.value()).build(),
                request.getRequestURI());
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
