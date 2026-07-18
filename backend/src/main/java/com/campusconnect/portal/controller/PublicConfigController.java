package com.campusconnect.portal.controller;

import com.campusconnect.portal.common.response.ApiResponse;
import com.campusconnect.portal.config.props.OAuthProperties;
import com.campusconnect.portal.dto.config.PublicConfigResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves public, non-secret runtime configuration to the web app. Everything under
 * {@code /api/v1/public/**} is permit-all in the security config. This exists so the frontend
 * keeps a single build-time setting (its API base URL) and pulls everything else — like the
 * Google OAuth client id — from the backend at runtime.
 */
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@SecurityRequirements // no bearer token required
@Tag(name = "Public configuration", description = "Non-secret settings the web app reads before login")
public class PublicConfigController {

    private final OAuthProperties oAuthProperties;

    @Operation(summary = "Runtime web configuration",
            description = "Returns non-secret settings the frontend needs before authentication, "
                    + "such as the Google OAuth client id. Blank fields mean the feature is disabled.")
    @GetMapping("/config")
    public ApiResponse<PublicConfigResponse> config() {
        OAuthProperties.Google google = oAuthProperties.google();
        String clientId = (google != null && google.enabled()) ? google.clientId() : null;
        return ApiResponse.success(new PublicConfigResponse(clientId));
    }
}
