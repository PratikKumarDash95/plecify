package com.campusconnect.portal.controller;

import com.campusconnect.portal.common.response.ApiResponse;
import com.campusconnect.portal.dto.auth.AuthResponse;
import com.campusconnect.portal.dto.auth.ForgotPasswordRequest;
import com.campusconnect.portal.dto.auth.GoogleLoginRequest;
import com.campusconnect.portal.dto.auth.LoginChallengeResponse;
import com.campusconnect.portal.dto.auth.LoginRequest;
import com.campusconnect.portal.dto.auth.LoginResponse;
import com.campusconnect.portal.dto.auth.MessageResponse;
import com.campusconnect.portal.dto.auth.RefreshTokenRequest;
import com.campusconnect.portal.dto.auth.RefreshTokenResponse;
import com.campusconnect.portal.dto.auth.RegisterCompanyRequest;
import com.campusconnect.portal.dto.auth.RegisterStudentRequest;
import com.campusconnect.portal.dto.auth.ResendOtpRequest;
import com.campusconnect.portal.dto.auth.ResendVerificationRequest;
import com.campusconnect.portal.dto.auth.ResetPasswordRequest;
import com.campusconnect.portal.dto.auth.VerifyEmailRequest;
import com.campusconnect.portal.dto.auth.VerifyOtpRequest;
import com.campusconnect.portal.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public authentication and account-lifecycle endpoints. All routes under
 * {@code /api/v1/auth/**} are permit-all in the security config; authorization for the rest
 * of the API is driven by the JWT these endpoints issue.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@SecurityRequirements // no bearer token required for this controller
@Tag(name = "Authentication", description = "Registration, login, token refresh, and account recovery")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a student",
            description = "Creates a disabled student account and emails a verification link. "
                    + "No tokens are issued until the email is verified.")
    @PostMapping("/register/student")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> registerStudent(@Valid @RequestBody RegisterStudentRequest request) {
        AuthResponse response = authService.registerStudent(request);
        return ApiResponse.success(response, response.message());
    }

    @Operation(summary = "Register a company",
            description = "Creates a disabled company account (pending admin approval) and emails "
                    + "a verification link.")
    @PostMapping("/register/company")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> registerCompany(@Valid @RequestBody RegisterCompanyRequest request) {
        AuthResponse response = authService.registerCompany(request);
        return ApiResponse.success(response, response.message());
    }

    @Operation(summary = "Log in (step 1 of 2)",
            description = "Verifies credentials and emails a one-time code. No tokens are issued "
                    + "here — call /verify-otp with the code to complete the login.")
    @PostMapping("/login")
    public ApiResponse<LoginChallengeResponse> login(@Valid @RequestBody LoginRequest request,
                                                     HttpServletRequest servletRequest) {
        LoginChallengeResponse response = authService.login(request,
                servletRequest.getHeader("User-Agent"), clientIp(servletRequest));
        return ApiResponse.success(response,
                "A verification code has been sent to your email");
    }

    @Operation(summary = "Verify login code (step 2 of 2)",
            description = "Consumes the emailed one-time code and returns an access/refresh token pair.")
    @PostMapping("/verify-otp")
    public ApiResponse<LoginResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request,
                                                HttpServletRequest servletRequest) {
        LoginResponse response = authService.verifyLoginOtp(request.email(), request.code(),
                servletRequest.getHeader("User-Agent"), clientIp(servletRequest));
        return ApiResponse.success(response, "Login successful");
    }

    @Operation(summary = "Resend login code",
            description = "Re-issues the one-time login code. Responds identically whether or not "
                    + "the email is eligible, to avoid account enumeration.")
    @PostMapping("/resend-otp")
    public ApiResponse<Void> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authService.resendLoginOtp(request.email());
        return ApiResponse.message("If that login is in progress, a new code has been sent.");
    }

    @Operation(summary = "Log in with Google",
            description = "Verifies a Google ID token and returns an access/refresh token pair. "
                    + "A first-time Google email provisions an enabled STUDENT account with no "
                    + "password; the student profile is completed after first login.")
    @PostMapping("/google")
    public ApiResponse<LoginResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request,
                                                      HttpServletRequest servletRequest) {
        LoginResponse response = authService.loginWithGoogle(request,
                servletRequest.getHeader("User-Agent"), clientIp(servletRequest));
        return ApiResponse.success(response, "Login successful");
    }

    @Operation(summary = "Refresh tokens",
            description = "Rotates a refresh token, returning a new access/refresh pair. The "
                    + "presented refresh token is invalidated.")
    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request,
                                                     HttpServletRequest servletRequest) {
        RefreshTokenResponse response = authService.refresh(request.refreshToken(),
                servletRequest.getHeader("User-Agent"), clientIp(servletRequest));
        return ApiResponse.success(response, "Token refreshed");
    }

    @Operation(summary = "Log out",
            description = "Deletes the presented refresh token. Idempotent.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ApiResponse.message("Logged out");
    }

    @Operation(summary = "Verify email (link)",
            description = "Consumes the verification token from the emailed link and enables the account.")
    @GetMapping("/verify-email")
    public ApiResponse<MessageResponse> verifyEmail(
            @RequestParam("token") @NotBlank String token) {
        authService.verifyEmail(token);
        return ApiResponse.success(
                MessageResponse.of("Your email has been verified. You can now log in."),
                "Email verified");
    }

    @Operation(summary = "Verify email (body)",
            description = "POST variant of email verification for clients that prefer a request body.")
    @PostMapping("/verify-email")
    public ApiResponse<MessageResponse> verifyEmailByBody(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.token());
        return ApiResponse.success(
                MessageResponse.of("Your email has been verified. You can now log in."),
                "Email verified");
    }

    @Operation(summary = "Resend verification email",
            description = "Re-issues a verification link. Responds identically whether or not the "
                    + "email exists, to avoid account enumeration.")
    @PostMapping("/resend-verification")
    public ApiResponse<Void> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request.email());
        return ApiResponse.message("If that account exists and is unverified, a new link has been sent.");
    }

    @Operation(summary = "Request password reset",
            description = "Emails a reset link. Responds identically whether or not the email "
                    + "exists, to avoid account enumeration.")
    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
        return ApiResponse.message("If that account exists, a password reset link has been sent.");
    }

    @Operation(summary = "Reset password",
            description = "Consumes a reset token, sets the new password, and revokes existing sessions.")
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ApiResponse.message("Your password has been reset. Please log in with your new password.");
    }

    /**
     * Best-effort client IP: prefers the first hop in {@code X-Forwarded-For} when present
     * (behind a proxy/load balancer), otherwise the direct remote address.
     */
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
