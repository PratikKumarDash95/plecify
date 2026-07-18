package com.campusconnect.portal.service.auth;

import com.campusconnect.portal.common.enums.ApprovalStatus;
import com.campusconnect.portal.common.enums.AuthProvider;
import com.campusconnect.portal.common.enums.RoleType;
import com.campusconnect.portal.common.enums.TokenType;
import com.campusconnect.portal.config.props.EmailProperties;
import com.campusconnect.portal.config.props.SecurityProperties;
import com.campusconnect.portal.dto.auth.AuthResponse;
import com.campusconnect.portal.dto.auth.LoginChallengeResponse;
import com.campusconnect.portal.dto.auth.GoogleLoginRequest;
import com.campusconnect.portal.dto.auth.LoginRequest;
import com.campusconnect.portal.dto.auth.LoginResponse;
import com.campusconnect.portal.dto.auth.RefreshTokenResponse;
import com.campusconnect.portal.dto.auth.RegisterCompanyRequest;
import com.campusconnect.portal.dto.auth.RegisterStudentRequest;
import com.campusconnect.portal.dto.auth.UserResponse;
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
import com.campusconnect.portal.security.LoginSessionCache;
import com.campusconnect.portal.security.SessionCacheEvictor;
import com.campusconnect.portal.service.email.EmailService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private UniversityRepository universityRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private GoogleTokenVerifier googleTokenVerifier;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private VerificationTokenService verificationTokenService;
    @Mock private EmailService emailService;
    @Mock private EmailProperties emailProperties;
    @Mock private SecurityProperties securityProperties;
    @Mock private UserMapper userMapper;
    @Mock private LoginSessionCache loginSessionCache;
    @Mock private SessionCacheEvictor sessionCacheEvictor;

    @InjectMocks private AuthServiceImpl authService;

    private static final UUID UNIVERSITY_ID = UUID.randomUUID();
    private static final String UNIVERSITY_DOMAIN = "uni.edu.in";

    @BeforeEach
    void setUp() {
        lenient().when(emailProperties.frontendUrl()).thenReturn("http://localhost:3000");
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("$2a$hashed");
        lenient().when(verificationTokenService.issue(any(), any())).thenReturn("raw-verify-token");
        lenient().when(verificationTokenService.issueLoginOtp(any())).thenReturn("123456");
        lenient().when(securityProperties.otpExpirationMs()).thenReturn(600000L);
    }

    // ------------------------------------------------------------------ registerStudent

    @Test
    void registerStudentCreatesDisabledAccountAndSendsVerification() {
        RegisterStudentRequest request = studentRequest();
        University university = activeUniversity();
        when(universityRepository.findByEmailDomainIgnoreCase(UNIVERSITY_DOMAIN)).thenReturn(Optional.of(university));
        when(userRepository.existsByEmailIgnoreCase("new@uni.edu")).thenReturn(false);
        when(studentRepository.existsByUniversityIdAndRollNumberIgnoreCase(UNIVERSITY_ID, "R-101"))
                .thenReturn(false);
        when(roleRepository.findByName(RoleType.STUDENT))
                .thenReturn(Optional.of(role(RoleType.STUDENT)));
        when(userMapper.toResponse(any())).thenReturn(sampleUserResponse());

        AuthResponse response = authService.registerStudent(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.isEnabled()).isFalse();
        assertThat(saved.isEmailVerified()).isFalse();
        assertThat(saved.getEmail()).isEqualTo("new@uni.edu");
        assertThat(saved.getPasswordHash()).isEqualTo("$2a$hashed");
        assertThat(saved.getRoles()).extracting(Role::getName).containsExactly(RoleType.STUDENT);

        verify(studentRepository).save(any(Student.class));
        verify(verificationTokenService).issue(saved, TokenType.EMAIL_VERIFICATION);
        verify(emailService).sendEmailVerification(eq("new@uni.edu"), any(),
                eq("http://localhost:3000/verify-email?token=raw-verify-token"));
        assertThat(response.user()).isNotNull();
    }

    @Test
    void registerStudentNormalizesEmailAndSkills() {
        RegisterStudentRequest request = new RegisterStudentRequest(
                "  MixedCase@Uni.EDU ", "password1", "Ada Lovelace", null,
                UNIVERSITY_DOMAIN, "R-101", "CSE", "CS", "B.Tech",
                new BigDecimal("8.5"), 0, 0, 2026, null, "Remote",
                new HashSet<>(Set.of("Java", " java ", "SQL", "")));
        when(universityRepository.findByEmailDomainIgnoreCase(UNIVERSITY_DOMAIN)).thenReturn(Optional.of(activeUniversity()));
        when(userRepository.existsByEmailIgnoreCase("mixedcase@uni.edu")).thenReturn(false);
        when(studentRepository.existsByUniversityIdAndRollNumberIgnoreCase(any(), any()))
                .thenReturn(false);
        when(roleRepository.findByName(RoleType.STUDENT))
                .thenReturn(Optional.of(role(RoleType.STUDENT)));
        when(userMapper.toResponse(any())).thenReturn(sampleUserResponse());

        authService.registerStudent(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("mixedcase@uni.edu");

        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(studentCaptor.capture());
        // Blanks dropped, case-folded, duplicates collapsed.
        assertThat(studentCaptor.getValue().getSkills()).containsExactlyInAnyOrder("java", "sql");
    }

    @Test
    void registerStudentRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("new@uni.edu")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerStudent(studentRequest()))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendEmailVerification(any(), any(), any());
    }

    @Test
    void registerStudentRejectsUnknownUniversity() {
        when(userRepository.existsByEmailIgnoreCase("new@uni.edu")).thenReturn(false);
        when(universityRepository.findByEmailDomainIgnoreCase(UNIVERSITY_DOMAIN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.registerStudent(studentRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void registerStudentRejectsInactiveUniversity() {
        University inactive = activeUniversity();
        inactive.setActive(false);
        when(userRepository.existsByEmailIgnoreCase("new@uni.edu")).thenReturn(false);
        when(universityRepository.findByEmailDomainIgnoreCase(UNIVERSITY_DOMAIN)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> authService.registerStudent(studentRequest()))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }

    @Test
    void registerStudentRejectsDuplicateRollNumber() {
        when(userRepository.existsByEmailIgnoreCase("new@uni.edu")).thenReturn(false);
        when(universityRepository.findByEmailDomainIgnoreCase(UNIVERSITY_DOMAIN)).thenReturn(Optional.of(activeUniversity()));
        when(studentRepository.existsByUniversityIdAndRollNumberIgnoreCase(UNIVERSITY_ID, "R-101"))
                .thenReturn(true);

        assertThatThrownBy(() -> authService.registerStudent(studentRequest()))
                .isInstanceOf(ResourceAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ registerCompany

    @Test
    void registerCompanyCreatesPendingProfile() {
        RegisterCompanyRequest request = new RegisterCompanyRequest(
                "hr@acme.com", "password1", "Rec Ruiter", "555",
                "Acme Corp", "Tech", "https://acme.com", "desc", "NYC",
                null, "555-1234");
        when(userRepository.existsByEmailIgnoreCase("hr@acme.com")).thenReturn(false);
        when(roleRepository.findByName(RoleType.COMPANY))
                .thenReturn(Optional.of(role(RoleType.COMPANY)));
        when(userMapper.toResponse(any())).thenReturn(sampleUserResponse());

        authService.registerCompany(request);

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(companyCaptor.capture());
        Company company = companyCaptor.getValue();
        assertThat(company.getStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(company.getName()).isEqualTo("Acme Corp");
        // Contact email defaults to the login email when not supplied.
        assertThat(company.getContactEmail()).isEqualTo("hr@acme.com");
        verify(emailService).sendEmailVerification(eq("hr@acme.com"), any(), anyString());
    }

    // ------------------------------------------------------------------ login

    @Test
    void loginEmailsOtpAndIssuesNoTokensForValidVerifiedUser() {
        User user = enabledUser();
        when(userRepository.findByEmailIgnoreCase("user@uni.edu")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password1", "$2a$hashed")).thenReturn(true);

        LoginChallengeResponse response = authService.login(
                new LoginRequest("user@uni.edu", "password1"), "agent", "1.2.3.4");

        assertThat(response.otpRequired()).isTrue();
        assertThat(response.email()).isEqualTo("user@uni.edu");
        assertThat(response.expiresIn()).isEqualTo(600L);
        // Step one emails a code but must not mint any tokens.
        verify(verificationTokenService).issueLoginOtp(user);
        verify(emailService).sendLoginOtp(eq("user@uni.edu"), any(), eq("123456"), eq(10L));
        verify(refreshTokenService, never()).issue(any(), any(), any());
        verify(jwtService, never()).generateAccessToken(any(), any(), any());
    }

    @Test
    void verifyLoginOtpIssuesTokenPairAndConsumesCode() {
        User user = enabledUser();
        when(userRepository.findByEmailIgnoreCase("user@uni.edu")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(eq(user.getId()), any(), any())).thenReturn("access-jwt");
        when(jwtService.getAccessTokenTtlSeconds()).thenReturn(900L);
        when(refreshTokenService.issue(user, "agent", "1.2.3.4")).thenReturn("refresh-raw");
        when(userMapper.toResponse(user)).thenReturn(sampleUserResponse());

        LoginResponse response = authService.verifyLoginOtp(
                "user@uni.edu", "123456", "agent", "1.2.3.4");

        verify(verificationTokenService).consumeLoginOtp(user, "123456");
        assertThat(response.accessToken()).isEqualTo("access-jwt");
        assertThat(response.refreshToken()).isEqualTo("refresh-raw");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900L);
        assertThat(response.role()).isEqualTo(RoleType.STUDENT.name());
    }

    @Test
    void verifyLoginOtpRejectsUnknownEmail() {
        when(userRepository.findByEmailIgnoreCase("ghost@uni.edu")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyLoginOtp("ghost@uni.edu", "123456", null, null))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(ErrorCode.TOKEN_INVALID);
        verify(refreshTokenService, never()).issue(any(), any(), any());
    }

    @Test
    void verifyLoginOtpRejectsLockedAccountBeforeConsumingCode() {
        User user = enabledUser();
        user.setAccountLocked(true);
        when(userRepository.findByEmailIgnoreCase("user@uni.edu")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.verifyLoginOtp("user@uni.edu", "123456", null, null))
                .isInstanceOf(LockedException.class);
        verify(verificationTokenService, never()).consumeLoginOtp(any(), any());
        verify(refreshTokenService, never()).issue(any(), any(), any());
    }

    @Test
    void loginRejectsUnknownEmailWithBadCredentials() {
        when(userRepository.findByEmailIgnoreCase("ghost@uni.edu")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("ghost@uni.edu", "password1"), null, null))
                .isInstanceOf(BadCredentialsException.class);
        verify(refreshTokenService, never()).issue(any(), any(), any());
    }

    @Test
    void loginRejectsWrongPassword() {
        User user = enabledUser();
        when(userRepository.findByEmailIgnoreCase("user@uni.edu")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "$2a$hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("user@uni.edu", "wrong"), null, null))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void loginRejectsLockedAccount() {
        User user = enabledUser();
        user.setAccountLocked(true);
        when(userRepository.findByEmailIgnoreCase("user@uni.edu")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("user@uni.edu", "password1"), null, null))
                .isInstanceOf(LockedException.class);
    }

    @Test
    void loginRejectsUnverifiedEmail() {
        User user = enabledUser();
        user.setEmailVerified(false);
        when(userRepository.findByEmailIgnoreCase("user@uni.edu")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("user@uni.edu", "password1"), null, null))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED);
    }

    @Test
    void loginRejectsDisabledAccount() {
        User user = enabledUser();
        user.setEnabled(false);
        when(userRepository.findByEmailIgnoreCase("user@uni.edu")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("user@uni.edu", "password1"), null, null))
                .isInstanceOf(DisabledException.class);
    }

    @Test
    void loginRejectsGoogleOnlyAccountWithBadCredentials() {
        User user = enabledUser();
        user.setPasswordHash(null); // federated-only account
        when(userRepository.findByEmailIgnoreCase("user@uni.edu")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("user@uni.edu", "password1"), null, null))
                .isInstanceOf(BadCredentialsException.class);
        verify(passwordEncoder, never()).matches(any(), any());
        verify(refreshTokenService, never()).issue(any(), any(), any());
    }

    // ------------------------------------------------------------------ loginWithGoogle

    @Test
    void googleLoginProvisionsNewStudentWhenEmailIsUnknown() {
        GoogleIdToken.Payload payload = googlePayload("new.grad@gmail.com", "New Grad", "google-sub-123");
        when(googleTokenVerifier.verify("id-token")).thenReturn(payload);
        when(userRepository.findByEmailIgnoreCase("new.grad@gmail.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName(RoleType.STUDENT))
                .thenReturn(Optional.of(role(RoleType.STUDENT)));
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("access-jwt");
        when(jwtService.getAccessTokenTtlSeconds()).thenReturn(900L);
        when(refreshTokenService.issue(any(), any(), any())).thenReturn("refresh-raw");
        when(userMapper.toResponse(any())).thenReturn(sampleUserResponse());

        LoginResponse response = authService.loginWithGoogle(
                new GoogleLoginRequest("id-token"), "agent", "1.2.3.4");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("new.grad@gmail.com");
        assertThat(saved.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(saved.getProviderId()).isEqualTo("google-sub-123");
        assertThat(saved.getPasswordHash()).isNull();
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.isEmailVerified()).isTrue();
        assertThat(saved.getRoles()).extracting(Role::getName).containsExactly(RoleType.STUDENT);
        assertThat(response.accessToken()).isEqualTo("access-jwt");
        assertThat(response.refreshToken()).isEqualTo("refresh-raw");
    }

    @Test
    void googleLoginLinksExistingAccountWithoutCreatingANewOne() {
        User existing = enabledUser();
        GoogleIdToken.Payload payload = googlePayload("user@uni.edu", "Verified User", "google-sub-999");
        when(googleTokenVerifier.verify("id-token")).thenReturn(payload);
        when(userRepository.findByEmailIgnoreCase("user@uni.edu")).thenReturn(Optional.of(existing));
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("access-jwt");
        when(jwtService.getAccessTokenTtlSeconds()).thenReturn(900L);
        when(refreshTokenService.issue(any(), any(), any())).thenReturn("refresh-raw");
        when(userMapper.toResponse(existing)).thenReturn(sampleUserResponse());

        LoginResponse response = authService.loginWithGoogle(
                new GoogleLoginRequest("id-token"), null, null);

        assertThat(existing.getProviderId()).isEqualTo("google-sub-999");
        assertThat(response.accessToken()).isEqualTo("access-jwt");
    }

    @Test
    void googleLoginRejectsUnverifiedGoogleEmail() {
        GoogleIdToken.Payload payload = googlePayload("user@uni.edu", "User", "sub");
        payload.setEmailVerified(false);
        when(googleTokenVerifier.verify("id-token")).thenReturn(payload);

        assertThatThrownBy(() -> authService.loginWithGoogle(
                new GoogleLoginRequest("id-token"), null, null))
                .isInstanceOf(ApiException.class)
                .extracting(e -> ((ApiException) e).getErrorCode())
                .isEqualTo(ErrorCode.AUTHENTICATION_FAILED);
        verify(refreshTokenService, never()).issue(any(), any(), any());
    }

    // ------------------------------------------------------------------ refresh / logout

    @Test
    void refreshRotatesTokenAndIssuesNewAccessToken() {
        User user = enabledUser();
        when(refreshTokenService.rotate("old-refresh", "agent", "1.2.3.4"))
                .thenReturn(new RefreshTokenService.RotationResult(user, "new-refresh"));
        when(jwtService.generateAccessToken(eq(user.getId()), any(), any())).thenReturn("new-access");
        when(jwtService.getAccessTokenTtlSeconds()).thenReturn(900L);

        RefreshTokenResponse response = authService.refresh("old-refresh", "agent", "1.2.3.4");

        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void logoutDelegatesToRefreshTokenService() {
        authService.logout("some-refresh");
        verify(refreshTokenService).deleteByRawToken("some-refresh");
    }

    // ------------------------------------------------------------------ email verification

    @Test
    void verifyEmailEnablesAccount() {
        User user = enabledUser();
        user.setEmailVerified(false);
        user.setEnabled(false);
        when(verificationTokenService.consume("tok", TokenType.EMAIL_VERIFICATION)).thenReturn(user);

        authService.verifyEmail("tok");

        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void resendVerificationIsSilentForUnknownEmail() {
        when(userRepository.findByEmailIgnoreCase("ghost@uni.edu")).thenReturn(Optional.empty());

        authService.resendVerification("ghost@uni.edu");

        verify(verificationTokenService, never()).issue(any(), any());
        verify(emailService, never()).sendEmailVerification(any(), any(), any());
    }

    @Test
    void resendVerificationSkipsAlreadyVerifiedAccount() {
        User user = enabledUser(); // emailVerified == true
        when(userRepository.findByEmailIgnoreCase("user@uni.edu")).thenReturn(Optional.of(user));

        authService.resendVerification("user@uni.edu");

        verify(emailService, never()).sendEmailVerification(any(), any(), any());
    }

    // ------------------------------------------------------------------ password reset

    @Test
    void forgotPasswordSendsResetLinkWhenAccountExists() {
        User user = enabledUser();
        when(userRepository.findByEmailIgnoreCase("user@uni.edu")).thenReturn(Optional.of(user));
        when(verificationTokenService.issue(user, TokenType.PASSWORD_RESET)).thenReturn("reset-raw");

        authService.forgotPassword("user@uni.edu");

        verify(emailService).sendPasswordReset(eq("user@uni.edu"), any(),
                eq("http://localhost:3000/reset-password?token=reset-raw"));
    }

    @Test
    void forgotPasswordIsSilentForUnknownEmail() {
        when(userRepository.findByEmailIgnoreCase("ghost@uni.edu")).thenReturn(Optional.empty());

        authService.forgotPassword("ghost@uni.edu");

        verify(verificationTokenService, never()).issue(any(), any());
        verify(emailService, never()).sendPasswordReset(any(), any(), any());
    }

    @Test
    void resetPasswordSetsNewHashAndRevokesSessions() {
        User user = enabledUser();
        when(verificationTokenService.consume("reset-tok", TokenType.PASSWORD_RESET)).thenReturn(user);
        when(passwordEncoder.encode("newPassword1")).thenReturn("$2a$newhash");

        authService.resetPassword("reset-tok", "newPassword1");

        assertThat(user.getPasswordHash()).isEqualTo("$2a$newhash");
        verify(refreshTokenService).revokeAll(user.getId());
    }

    // ------------------------------------------------------------------ fixtures

    private RegisterStudentRequest studentRequest() {
        return new RegisterStudentRequest(
                "new@uni.edu", "password1", "New Student", "555",
                UNIVERSITY_DOMAIN, "R-101", "CSE", "CS", "B.Tech",
                new BigDecimal("8.5"), 0, 0, 2026, null, "Remote",
                new HashSet<>(Set.of("java")));
    }

    private University activeUniversity() {
        University university = new University();
        university.setId(UNIVERSITY_ID);
        university.setActive(true);
        return university;
    }

    private Role role(RoleType type) {
        Role role = new Role();
        role.setName(type);
        return role;
    }

    private User enabledUser() {
        Set<Role> roles = new HashSet<>();
        roles.add(role(RoleType.STUDENT));
        User user = User.builder()
                .email("user@uni.edu")
                .passwordHash("$2a$hashed")
                .fullName("Verified User")
                .enabled(true)
                .emailVerified(true)
                .accountLocked(false)
                .roles(roles)
                .build();
        user.setId(UUID.randomUUID());
        return user;
    }

    private GoogleIdToken.Payload googlePayload(String email, String name, String subject) {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail(email);
        payload.setEmailVerified(true);
        payload.setSubject(subject);
        payload.set("name", name);
        return payload;
    }

    private UserResponse sampleUserResponse() {
        return new UserResponse(UUID.randomUUID(), "user@uni.edu", "Verified User", null,
                true, true, Set.of(RoleType.STUDENT.name()), null);
    }
}
