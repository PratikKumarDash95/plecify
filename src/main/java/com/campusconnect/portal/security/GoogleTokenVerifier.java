package com.campusconnect.portal.security;

import com.campusconnect.portal.config.props.OAuthProperties;
import com.campusconnect.portal.exception.ApiException;
import com.campusconnect.portal.exception.ErrorCode;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Verifies Google-issued ID tokens against Google's public keys and the configured client id.
 * A valid token proves the bearer's Google identity (email + subject); we never trust the
 * token's claims without this signature/audience/issuer check.
 */
@Slf4j
@Component
public class GoogleTokenVerifier {

    private final OAuthProperties oauthProperties;
    private GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(OAuthProperties oauthProperties) {
        this.oauthProperties = oauthProperties;
    }

    @PostConstruct
    void init() {
        OAuthProperties.Google google = oauthProperties.google();
        if (google == null || !google.enabled()) {
            log.info("Google OAuth is disabled; /auth/google will reject requests.");
            return;
        }
        if (google.clientId() == null || google.clientId().isBlank()) {
            log.warn("Google OAuth is enabled but app.oauth.google.client-id is not set; "
                    + "token verification will fail until it is configured.");
            return;
        }
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(google.clientId()))
                .build();
    }

    /**
     * Verifies the raw ID token and returns its payload.
     *
     * @throws ApiException with {@link ErrorCode#AUTHENTICATION_FAILED} if OAuth is not
     *         configured or the token is invalid, expired, or issued for another audience.
     */
    public Payload verify(String idToken) {
        if (verifier == null) {
            throw new ApiException(ErrorCode.AUTHENTICATION_FAILED, "Google sign-in is not configured");
        }
        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new ApiException(ErrorCode.AUTHENTICATION_FAILED, "Invalid Google credential");
            }
            return token.getPayload();
        } catch (GeneralSecurityException | IOException ex) {
            log.warn("Google ID token verification failed: {}", ex.getMessage());
            throw new ApiException(ErrorCode.AUTHENTICATION_FAILED, "Could not verify Google credential");
        }
    }
}
