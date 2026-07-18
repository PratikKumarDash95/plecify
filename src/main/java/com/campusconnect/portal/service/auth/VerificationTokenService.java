package com.campusconnect.portal.service.auth;

import com.campusconnect.portal.common.enums.TokenType;
import com.campusconnect.portal.config.props.SecurityProperties;
import com.campusconnect.portal.entity.User;
import com.campusconnect.portal.entity.VerificationToken;
import com.campusconnect.portal.exception.ApiException;
import com.campusconnect.portal.exception.ErrorCode;
import com.campusconnect.portal.repository.VerificationTokenRepository;
import com.campusconnect.portal.security.SecureTokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Issues and consumes single-use security tokens for email verification and password reset.
 * Any outstanding token of the same type is invalidated before a new one is issued, so only
 * the most recent link is ever valid. Only the token hash is persisted.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationTokenService {

    private static final int OTP_DIGITS = 6;

    private final VerificationTokenRepository tokenRepository;
    private final SecureTokenGenerator tokenGenerator;
    private final SecurityProperties securityProperties;

    /**
     * Issues a fresh token of the given type, invalidating any outstanding ones first.
     *
     * @return the raw token to embed in the emailed link (never stored)
     */
    @Transactional
    public String issue(User user, TokenType type) {
        tokenRepository.invalidateOutstanding(user.getId(), type, Instant.now());

        String rawToken = tokenGenerator.generateToken();
        VerificationToken token = VerificationToken.builder()
                .user(user)
                .type(type)
                .tokenHash(tokenGenerator.hash(rawToken))
                .expiresAt(Instant.now().plusMillis(ttlFor(type)))
                .build();
        tokenRepository.save(token);
        return rawToken;
    }

    /**
     * Issues a fresh 6-digit login OTP for the user, removing any prior OTP first. The stored
     * hash binds the code to the user ({@code userId:code}) so low-entropy codes can never
     * collide across accounts, and lookup at verification time is scoped to the same user.
     *
     * @return the raw 6-digit code to email (never stored)
     */
    @Transactional
    public String issueLoginOtp(User user) {
        // OTPs are low-entropy; keep exactly one live code per user by hard-deleting prior ones.
        tokenRepository.deleteByUserIdAndType(user.getId(), TokenType.LOGIN_OTP);

        String code = tokenGenerator.generateNumericCode(OTP_DIGITS);
        VerificationToken token = VerificationToken.builder()
                .user(user)
                .type(TokenType.LOGIN_OTP)
                .tokenHash(tokenGenerator.hash(user.getId() + ":" + code))
                .expiresAt(Instant.now().plusMillis(securityProperties.otpExpirationMs()))
                .build();
        tokenRepository.save(token);
        return code;
    }

    /**
     * Validates and consumes a login OTP for the given user. Because codes are bound to the user
     * in their hash, lookup uses the {@code userId:code} composite.
     *
     * @throws ApiException {@code TOKEN_INVALID} if the code doesn't match this user,
     *         {@code TOKEN_EXPIRED} if expired or already consumed
     */
    @Transactional
    public void consumeLoginOtp(User user, String code) {
        VerificationToken token = tokenRepository
                .findByTokenHash(tokenGenerator.hash(user.getId() + ":" + code))
                .orElseThrow(() -> new ApiException(ErrorCode.TOKEN_INVALID));

        if (token.getType() != TokenType.LOGIN_OTP || !token.getUser().getId().equals(user.getId())) {
            throw new ApiException(ErrorCode.TOKEN_INVALID);
        }
        if (!token.isUsable()) {
            throw new ApiException(ErrorCode.TOKEN_EXPIRED);
        }
        token.setUsedAt(Instant.now());
    }

    /**
     * Validates and consumes a token of the expected type, returning its owning user.
     * A token is usable only if it exists, matches the type, is unexpired, and unused.
     *
     * @throws ApiException {@code TOKEN_INVALID} if unknown or of the wrong type,
     *         {@code TOKEN_EXPIRED} if expired or already consumed
     */
    @Transactional
    public User consume(String rawToken, TokenType expectedType) {
        VerificationToken token = tokenRepository.findByTokenHash(tokenGenerator.hash(rawToken))
                .orElseThrow(() -> new ApiException(ErrorCode.TOKEN_INVALID));

        if (token.getType() != expectedType) {
            throw new ApiException(ErrorCode.TOKEN_INVALID);
        }
        if (!token.isUsable()) {
            throw new ApiException(ErrorCode.TOKEN_EXPIRED);
        }

        token.setUsedAt(Instant.now());
        return token.getUser();
    }

    private long ttlFor(TokenType type) {
        return switch (type) {
            case EMAIL_VERIFICATION -> securityProperties.verificationTokenExpirationMs();
            case PASSWORD_RESET -> securityProperties.resetTokenExpirationMs();
            case LOGIN_OTP -> securityProperties.otpExpirationMs();
        };
    }
}
