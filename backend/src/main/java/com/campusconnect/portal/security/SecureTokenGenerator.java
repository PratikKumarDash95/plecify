package com.campusconnect.portal.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Generates high-entropy, URL-safe opaque tokens (for refresh tokens, email verification,
 * and password reset) and derives their SHA-256 hash for at-rest storage. The raw token is
 * returned to the caller exactly once and never persisted; only the hash is stored, so a
 * database leak cannot be replayed against these flows.
 */
@Component
public class SecureTokenGenerator {

    /** 32 bytes = 256 bits of entropy, encoded to a 43-char URL-safe string. */
    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder urlEncoder = Base64.getUrlEncoder().withoutPadding();

    /** @return a fresh, cryptographically random URL-safe token. */
    public String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return urlEncoder.encodeToString(bytes);
    }

    /**
     * Generates a numeric one-time code (e.g. a 6-digit login OTP), zero-padded to the requested
     * length. Uses {@link SecureRandom}; low-entropy by design, so callers must pair it with a
     * short TTL, single-use consumption, and rate limiting.
     *
     * @param digits number of digits (2–9)
     * @return a zero-padded numeric string of exactly {@code digits} characters
     */
    public String generateNumericCode(int digits) {
        if (digits < 2 || digits > 9) {
            throw new IllegalArgumentException("digits must be between 2 and 9");
        }
        int bound = (int) Math.pow(10, digits);
        int value = secureRandom.nextInt(bound);
        return String.format("%0" + digits + "d", value);
    }

    /**
     * Derives the storage hash for a raw token. Deterministic: the same input always yields
     * the same hash, enabling constant-shape lookups by hash.
     *
     * @param rawToken the opaque token as issued to the client
     * @return lowercase hex-encoded SHA-256 digest (64 chars)
     */
    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated by the JLS; absence is a fatal, non-recoverable environment fault.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
