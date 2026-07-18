package com.campusconnect.portal.service.auth;

import com.campusconnect.portal.common.enums.ApprovalStatus;
import com.campusconnect.portal.common.enums.AuthProvider;
import com.campusconnect.portal.common.enums.RoleType;
import com.campusconnect.portal.common.enums.TokenType;
import com.campusconnect.portal.common.enums.WorkAuthorization;
import com.campusconnect.portal.config.props.EmailProperties;
import com.campusconnect.portal.config.props.SecurityProperties;
import com.campusconnect.portal.dto.auth.AuthResponse;
import com.campusconnect.portal.dto.auth.GoogleLoginRequest;
import com.campusconnect.portal.dto.auth.LoginChallengeResponse;
import com.campusconnect.portal.dto.auth.LoginRequest;
import com.campusconnect.portal.dto.auth.LoginResponse;
import com.campusconnect.portal.dto.auth.RefreshTokenResponse;
import com.campusconnect.portal.dto.auth.RegisterCompanyRequest;
import com.campusconnect.portal.dto.auth.RegisterStudentRequest;
import com.campusconnect.portal.entity.Company;
import com.campusconnect.portal.entity.Role;
import com.campusconnect.portal.entity.Student;
import com.campusconnect.portal.entity.University;
import com.campusconnect.portal.entity.User;
import com.campusconnect.portal.exception.ApiException;
import com.campusconnect.portal.exception.ErrorCode;
import com.campusconnect.portal.exception.ResourceAlreadyExistsException;
import com.campusconnect.portal.exception.ResourceNotFoundException;
import com.campusconnect.portal.mapper.UserMapper;
import com.campusconnect.portal.repository.CompanyRepository;
import com.campusconnect.portal.repository.RoleRepository;
import com.campusconnect.portal.repository.StudentRepository;
import com.campusconnect.portal.repository.UniversityRepository;
import com.campusconnect.portal.repository.UserRepository;
import com.campusconnect.portal.security.GoogleTokenVerifier;
import com.campusconnect.portal.security.JwtService;
import com.campusconnect.portal.service.email.EmailService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default {@link AuthService}. Owns account creation, credential verification, token issuance,
 * and the email-driven verification/reset flows.
 *
 * <p>Security posture:
 * <ul>
 *   <li>Passwords are BCrypt-hashed (encoder configured in security config).</li>
 *   <li>New accounts are created disabled and enabled only after email verification.</li>
 *   <li>{@code forgotPassword}/{@code resendVerification} never reveal whether an email exists
 *       (no account enumeration).</li>
 *   <li>Refresh tokens are opaque, rotating, and revoked on password reset.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final UniversityRepository universityRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final RefreshTokenService refreshTokenService;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final EmailProperties emailProperties;
    private final SecurityProperties securityProperties;
    private final UserMapper userMapper;

    // ------------------------------------------------------------------ registration

    @Override
    @Transactional
    public AuthResponse registerStudent(RegisterStudentRequest request) {
        String email = normalizeEmail(request.email());
        guardEmailAvailable(email);

        University university = universityRepository.findByEmailDomainIgnoreCase(request.universityDomain())
                .orElseThrow(() -> new ResourceNotFoundException("University", request.universityDomain()));
        if (!university.isActive()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "University is not accepting registrations");
        }
        if (studentRepository.existsByUniversityIdAndRollNumberIgnoreCase(
                university.getId(), request.rollNumber())) {
            throw new ResourceAlreadyExistsException("Student", "roll number", request.rollNumber());
        }

        User user = buildUser(email, request.password(), request.fullName(), request.phone(),
                RoleType.STUDENT);
        userRepository.save(user);

        Student student = Student.builder()
                .user(user)
                .university(university)
                .rollNumber(request.rollNumber())
                .department(request.department())
                .branch(request.branch())
                .degree(request.degree())
                .cgpa(request.cgpa())
                .activeBacklogs(request.activeBacklogs())
                .totalBacklogs(request.totalBacklogs())
                .passingYear(request.passingYear())
                .workAuthorization(request.workAuthorization() != null
                        ? request.workAuthorization() : WorkAuthorization.CITIZEN)
                .location(request.location())
                .skills(normalizeSkills(request.skills()))
                .build();
        studentRepository.save(student);

        sendVerificationEmail(user);
        log.info("Registered student account {} at university {}", user.getId(), university.getId());
        return new AuthResponse(userMapper.toResponse(user),
                "Registration successful. Please check your email to verify your account.");
    }

    @Override
    @Transactional
    public AuthResponse registerCompany(RegisterCompanyRequest request) {
        String email = normalizeEmail(request.email());
        guardEmailAvailable(email);

        User user = buildUser(email, request.password(), request.contactPersonName(),
                request.phone(), RoleType.COMPANY);
        userRepository.save(user);

        Company company = Company.builder()
                .user(user)
                .name(request.companyName())
                .industry(request.industry())
                .website(request.website())
                .description(request.description())
                .headquarters(request.headquarters())
                .contactEmail(request.contactEmail() != null ? request.contactEmail() : email)
                .contactPhone(request.contactPhone())
                .status(ApprovalStatus.PENDING)
                .build();
        companyRepository.save(company);

        sendVerificationEmail(user);
        log.info("Registered company account {} ({})", user.getId(), request.companyName());
        return new AuthResponse(userMapper.toResponse(user),
                "Registration successful. Verify your email, then an admin will review your company for approval.");
    }

    // ------------------------------------------------------------------ login / tokens

    @Override
    @Transactional
    public LoginChallengeResponse login(LoginRequest request, String userAgent, String ipAddress) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException(
                        ErrorCode.INVALID_CREDENTIALS.getDefaultMessage()));

        // Federated-only accounts have no local password; a password login must fail as if the
        // credentials were wrong (no hint that the email exists under a different provider).
        if (!user.hasPassword()
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException(ErrorCode.INVALID_CREDENTIALS.getDefaultMessage());
        }
        assertLoginable(user);

        // Credentials are good — issue a one-time code by email instead of tokens. The token pair
        // is only minted once the user proves control of the mailbox via verifyLoginOtp.
        String code = verificationTokenService.issueLoginOtp(user);
        emailService.sendLoginOtp(user.getEmail(), user.getFullName(), code, otpExpiryMinutes());
        log.info("Issued login OTP for user {}", user.getId());
        return LoginChallengeResponse.of(user.getEmail(), otpExpirySeconds());
    }

    @Override
    @Transactional
    public LoginResponse verifyLoginOtp(String email, String code, String userAgent, String ipAddress) {
        String normalized = normalizeEmail(email);
        User user = userRepository.findByEmailIgnoreCase(normalized)
                .orElseThrow(() -> new ApiException(ErrorCode.TOKEN_INVALID));

        // Re-validate account state: it could have been locked/disabled between the two steps.
        assertLoginable(user);
        verificationTokenService.consumeLoginOtp(user, code);

        log.info("User {} completed OTP login", user.getId());
        return issueTokens(user, userAgent, ipAddress);
    }

    @Override
    @Transactional
    public void resendLoginOtp(String email) {
        userRepository.findByEmailIgnoreCase(normalizeEmail(email)).ifPresent(user -> {
            if (user.hasPassword() && user.isEmailVerified() && user.isEnabled()
                    && !user.isAccountLocked()) {
                String code = verificationTokenService.issueLoginOtp(user);
                emailService.sendLoginOtp(user.getEmail(), user.getFullName(), code, otpExpiryMinutes());
            }
        });
        // Silent regardless of outcome — do not disclose whether the email exists or is eligible.
    }

    /**
     * Verifies an account is allowed to log in: email verified, enabled, and not locked.
     * Ordering mirrors the historical login path so error signals are unchanged.
     */
    private void assertLoginable(User user) {
        if (user.isAccountLocked()) {
            throw new LockedException(ErrorCode.AUTHENTICATION_FAILED.getDefaultMessage());
        }
        if (!user.isEmailVerified()) {
            throw new ApiException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        if (!user.isEnabled()) {
            throw new DisabledException(ErrorCode.AUTHENTICATION_FAILED.getDefaultMessage());
        }
    }

    @Override
    @Transactional
    public LoginResponse loginWithGoogle(GoogleLoginRequest request, String userAgent, String ipAddress) {
        Payload payload = googleTokenVerifier.verify(request.idToken());

        Boolean emailVerified = payload.getEmailVerified();
        if (emailVerified == null || !emailVerified) {
            throw new ApiException(ErrorCode.AUTHENTICATION_FAILED,
                    "Your Google account email is not verified");
        }

        String email = normalizeEmail(payload.getEmail());
        String googleSub = payload.getSubject();
        String name = (String) payload.get("name");

        User user = userRepository.findByEmailIgnoreCase(email)
                .map(existing -> linkGoogleIdentity(existing, googleSub))
                .orElseGet(() -> provisionGoogleUser(email, name, googleSub));

        if (user.isAccountLocked()) {
            throw new LockedException(ErrorCode.AUTHENTICATION_FAILED.getDefaultMessage());
        }
        if (!user.isEnabled()) {
            throw new DisabledException(ErrorCode.AUTHENTICATION_FAILED.getDefaultMessage());
        }

        log.info("User {} logged in via Google", user.getId());
        return issueTokens(user, userAgent, ipAddress);
    }

    @Override
    @Transactional
    public RefreshTokenResponse refresh(String refreshToken, String userAgent, String ipAddress) {
        RefreshTokenService.RotationResult result =
                refreshTokenService.rotate(refreshToken, userAgent, ipAddress);
        User user = result.user();
        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), roleNames(user));
        return RefreshTokenResponse.of(accessToken, result.rawToken(),
                jwtService.getAccessTokenTtlSeconds());
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.deleteByRawToken(refreshToken);
    }

    // ------------------------------------------------------------------ email verification

    @Override
    @Transactional
    public void verifyEmail(String token) {
        User user = verificationTokenService.consume(token, TokenType.EMAIL_VERIFICATION);
        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            user.setEnabled(true);
        }
        log.info("Verified email for user {}", user.getId());
    }

    @Override
    @Transactional
    public void resendVerification(String email) {
        userRepository.findByEmailIgnoreCase(normalizeEmail(email)).ifPresent(user -> {
            if (!user.isEmailVerified()) {
                sendVerificationEmail(user);
            }
        });
        // Silent regardless of outcome — do not disclose whether the email exists or is verified.
    }

    // ------------------------------------------------------------------ password reset

    @Override
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmailIgnoreCase(normalizeEmail(email)).ifPresent(user -> {
            String rawToken = verificationTokenService.issue(user, TokenType.PASSWORD_RESET);
            String resetUrl = buildFrontendUrl("/reset-password", rawToken);
            emailService.sendPasswordReset(user.getEmail(), user.getFullName(), resetUrl);
        });
        // Always succeeds from the caller's view — no account enumeration.
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = verificationTokenService.consume(token, TokenType.PASSWORD_RESET);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        // Any active sessions are invalidated so a leaked refresh token can't outlive the reset.
        refreshTokenService.revokeAll(user.getId());
        log.info("Password reset completed for user {}", user.getId());
    }

    // ------------------------------------------------------------------ helpers

    /** Issues an access/refresh pair for an authenticated user and builds the login response. */
    private LoginResponse issueTokens(User user, String userAgent, String ipAddress) {
        List<String> roles = roleNames(user);
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), roles);
        String refreshToken = refreshTokenService.issue(user, userAgent, ipAddress);
        return LoginResponse.of(accessToken, refreshToken, jwtService.getAccessTokenTtlSeconds(),
                primaryRole(roles), userMapper.toResponse(user));
    }

    /**
     * Links a Google identity to an existing account on first Google sign-in, recording the
     * provider subject so subsequent logins match on it. A pre-existing local account keeps its
     * password; we simply annotate that Google is now a valid sign-in for it.
     */
    private User linkGoogleIdentity(User user, String googleSub) {
        boolean changed = false;
        if (user.getProviderId() == null) {
            user.setProviderId(googleSub);
            changed = true;
        }
        // A Google-verified email is authoritative; enable and mark verified if it wasn't.
        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            user.setEnabled(true);
            changed = true;
        }
        if (changed) {
            userRepository.save(user);
        }
        return user;
    }

    /**
     * Provisions a first-time Google user as an enabled, email-verified STUDENT with no local
     * password and no student profile yet — the profile is completed after first login.
     */
    private User provisionGoogleUser(String email, String fullName, String googleSub) {
        Role role = roleRepository.findByName(RoleType.STUDENT)
                .orElseThrow(() -> new IllegalStateException(
                        "Role STUDENT is not seeded; check reference-data migration"));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        User user = User.builder()
                .email(email)
                .passwordHash(null)
                .fullName(fullName != null && !fullName.isBlank() ? fullName : email)
                .authProvider(AuthProvider.GOOGLE)
                .providerId(googleSub)
                .enabled(true)
                .emailVerified(true)
                .accountLocked(false)
                .roles(roles)
                .build();
        userRepository.save(user);
        log.info("Provisioned Google student account {} for {}", user.getId(), email);
        return user;
    }

    private User buildUser(String email, String rawPassword, String fullName, String phone,
                           RoleType roleType) {
        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new IllegalStateException(
                        "Role " + roleType + " is not seeded; check reference-data migration"));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        return User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .fullName(fullName)
                .phone(phone)
                .authProvider(AuthProvider.LOCAL)
                .enabled(false)
                .emailVerified(false)
                .accountLocked(false)
                .roles(roles)
                .build();
    }

    private void guardEmailAvailable(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResourceAlreadyExistsException("User", "email", email);
        }
    }

    private void sendVerificationEmail(User user) {
        String rawToken = verificationTokenService.issue(user, TokenType.EMAIL_VERIFICATION);
        String verifyUrl = buildFrontendUrl("/verify-email", rawToken);
        emailService.sendEmailVerification(user.getEmail(), user.getFullName(), verifyUrl);
    }

    private String buildFrontendUrl(String path, String token) {
        return UriComponentsBuilder.fromUriString(emailProperties.frontendUrl())
                .path(path)
                .queryParam("token", token)
                .build()
                .toUriString();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private long otpExpirySeconds() {
        return securityProperties.otpExpirationMs() / 1000;
    }

    private long otpExpiryMinutes() {
        return securityProperties.otpExpirationMs() / 60000;
    }

    private Set<String> normalizeSkills(Set<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return new HashSet<>();
        }
        return skills.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private List<String> roleNames(User user) {
        return user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();
    }

    /** The most privileged role, used as the login response's headline role. */
    private String primaryRole(List<String> roles) {
        for (RoleType type : RoleType.values()) {
            if (roles.contains(type.name())) {
                return type.name();
            }
        }
        return roles.isEmpty() ? RoleType.STUDENT.name() : roles.get(0);
    }
}
