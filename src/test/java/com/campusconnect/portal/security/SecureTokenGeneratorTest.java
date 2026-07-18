package com.campusconnect.portal.security;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SecureTokenGeneratorTest {

    private final SecureTokenGenerator generator = new SecureTokenGenerator();

    @Test
    void generatesUrlSafeTokenWithoutPadding() {
        String token = generator.generateToken();

        // 32 random bytes -> 43-char unpadded base64url, no '+', '/', or '=' characters.
        assertThat(token).hasSize(43);
        assertThat(token).matches("[A-Za-z0-9_-]+");
    }

    @Test
    void generatesDistinctTokensAcrossManyCalls() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            seen.add(generator.generateToken());
        }
        assertThat(seen).hasSize(1000);
    }

    @Test
    void hashIsDeterministicForSameInput() {
        String token = generator.generateToken();

        assertThat(generator.hash(token)).isEqualTo(generator.hash(token));
    }

    @Test
    void hashIsLowercaseHexOf256Bits() {
        String hash = generator.hash("any-token");

        assertThat(hash).hasSize(64);
        assertThat(hash).matches("[0-9a-f]{64}");
    }

    @Test
    void differentTokensProduceDifferentHashes() {
        assertThat(generator.hash("token-a")).isNotEqualTo(generator.hash("token-b"));
    }

    @Test
    void matchesKnownSha256Vector() {
        // SHA-256("abc") — canonical NIST test vector.
        assertThat(generator.hash("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }
}
