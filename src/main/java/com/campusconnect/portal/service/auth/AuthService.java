package com.campusconnect.portal.service.auth;

import com.campusconnect.portal.dto.auth.AuthResponse;
import com.campusconnect.portal.dto.auth.GoogleLoginRequest;
import com.campusconnect.portal.dto.auth.LoginChallengeResponse;
import com.campusconnect.portal.dto.auth.LoginRequest;
import com.campusconnect.portal.dto.auth.LoginResponse;
import com.campusconnect.portal.dto.auth.RefreshTokenResponse;
import com.campusconnect.portal.dto.auth.RegisterCompanyRequest;
import com.campusconnect.portal.dto.auth.RegisterStudentRequest;

/**
 * Authentication and account-lifecycle operations: self-registration (student, company),
 * login with JWT issuance, refresh-token rotation, logout, email verification, and the
 * password-reset flow.
 *
 * <p>Registration does not log the user in — accounts stay disabled until the emailed
 * verification link is followed.
 */
public interface AuthService {

    /**
     * Registers a new student account and its profile, then emails a verification link.
     * The account is created disabled until verification.
     */
    AuthResponse registerStudent(RegisterStudentRequest request);

    /**
     * Registers a new company account and its profile (status {@code PENDING} admin approval),
     * then emails a verification link.
     */
    AuthResponse registerCompany(RegisterCompanyRequest request);

    /**
     * First step of two-factor login: verifies credentials (and that the account is verified,
     * enabled, and unlocked), then emails a one-time code. No tokens are issued here — the client
     * completes the login via {@link #verifyLoginOtp}.
     *
     * @return a challenge describing that an OTP was emailed
     */
    LoginChallengeResponse login(LoginRequest request, String userAgent, String ipAddress);

    /**
     * Second step of two-factor login: consumes the emailed OTP for the given email and issues an
     * access/refresh token pair. Re-checks account state in case it changed since step one.
     *
     * @param userAgent originating User-Agent header (for token audit); may be {@code null}
     * @param ipAddress originating client IP (for token audit); may be {@code null}
     */
    LoginResponse verifyLoginOtp(String email, String code, String userAgent, String ipAddress);

    /**
     * Re-issues a login OTP for an in-progress two-step login. Silent (no error) if the email is
     * unknown or ineligible, to avoid disclosing account state.
     */
    void resendLoginOtp(String email);

    /**
     * Authenticates via a verified Google ID token and issues an access/refresh token pair.
     * A first-time Google email provisions a new enabled STUDENT account (email pre-verified,
     * no profile yet); the user completes their profile afterwards.
     *
     * @param userAgent originating User-Agent header (for token audit); may be {@code null}
     * @param ipAddress originating client IP (for token audit); may be {@code null}
     */
    LoginResponse loginWithGoogle(GoogleLoginRequest request, String userAgent, String ipAddress);

    /**
     * Rotates a refresh token, returning a new access/refresh pair and invalidating the old
     * refresh token.
     */
    RefreshTokenResponse refresh(String refreshToken, String userAgent, String ipAddress);

    /** Logs out by deleting the presented refresh token. Idempotent. */
    void logout(String refreshToken);

    /** Consumes an email-verification token, enabling the account. */
    void verifyEmail(String token);

    /** Re-issues a verification link for an unverified account. Silent if already verified. */
    void resendVerification(String email);

    /**
     * Starts the password-reset flow by emailing a reset link. Always succeeds from the
     * caller's perspective (no account enumeration), regardless of whether the email exists.
     */
    void forgotPassword(String email);

    /** Completes a password reset: consumes the token, sets the new password, revokes sessions. */
    void resetPassword(String token, String newPassword);
}
