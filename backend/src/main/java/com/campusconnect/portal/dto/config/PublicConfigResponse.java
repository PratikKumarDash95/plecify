package com.campusconnect.portal.dto.config;

/**
 * Public, non-secret runtime configuration the web app needs before a user authenticates.
 * Served by {@code GET /api/v1/public/config} so the frontend never has to bake these values
 * into its build. All fields here are safe to expose to anonymous clients.
 *
 * @param googleClientId the Google OAuth web client id, or {@code null}/blank when Google
 *                       sign-in is disabled (the frontend then hides the button)
 */
public record PublicConfigResponse(String googleClientId) {
}
