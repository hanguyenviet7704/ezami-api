package com.hth.udecareer.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PhpPasswordEncoder.
 * Tests password encoding and matching functionality for WordPress compatibility.
 */
class PhpPasswordEncoderTest {

    private PhpPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new PhpPasswordEncoder();
    }

    @Test
    void encode_shouldReturnNonNullHash() {
        // given
        String rawPassword = "testPassword123";

        // when
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // then
        assertNotNull(encodedPassword);
        assertFalse(encodedPassword.isEmpty());
    }

    @Test
    void encode_shouldReturnDifferentHashForSamePassword() {
        // given
        String rawPassword = "testPassword123";

        // when
        String hash1 = passwordEncoder.encode(rawPassword);
        String hash2 = passwordEncoder.encode(rawPassword);

        // then - hashes should be different due to random salt
        assertNotEquals(hash1, hash2, "Each encoding should produce different hash due to salt");
    }

    @Test
    void matches_withCorrectPassword_shouldReturnTrue() {
        // given
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // when
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

        // then
        assertTrue(matches);
    }

    @Test
    void matches_withWrongPassword_shouldReturnFalse() {
        // given
        String rawPassword = "testPassword123";
        String wrongPassword = "wrongPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // when
        boolean matches = passwordEncoder.matches(wrongPassword, encodedPassword);

        // then
        assertFalse(matches);
    }

    @Test
    void matches_withNullEncodedPassword_shouldReturnFalse() {
        // given
        String rawPassword = "testPassword123";

        // when
        boolean matches = passwordEncoder.matches(rawPassword, null);

        // then
        assertFalse(matches);
    }

    @Test
    void matches_withEmptyEncodedPassword_shouldReturnFalse() {
        // given
        String rawPassword = "testPassword123";

        // when
        boolean matches = passwordEncoder.matches(rawPassword, "");

        // then
        assertFalse(matches);
    }

    @Test
    void matches_withInvalidEncodedPassword_shouldReturnFalse() {
        // given
        String rawPassword = "testPassword123";
        String invalidEncodedPassword = "not-a-valid-hash";

        // when
        boolean matches = passwordEncoder.matches(rawPassword, invalidEncodedPassword);

        // then
        assertFalse(matches);
    }

    @Test
    void encode_withEmptyPassword_shouldReturnHash() {
        // given
        String rawPassword = "";

        // when
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // then
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches("", encodedPassword));
    }

    @Test
    void encode_withSpecialCharacters_shouldWorkCorrectly() {
        // given
        String rawPassword = "p@$$w0rd!#$%^&*()";

        // when
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // then
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void encode_withUnicodeCharacters_shouldWorkCorrectly() {
        // given
        String rawPassword = "mậtkhẩu密码パスワード";

        // when
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // then
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void encode_withLongPassword_shouldWorkCorrectly() {
        // given
        String rawPassword = "a".repeat(100);

        // when
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // then
        assertNotNull(encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void matches_isCaseSensitive() {
        // given
        String rawPassword = "TestPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // when
        boolean matchesLowerCase = passwordEncoder.matches("testpassword", encodedPassword);
        boolean matchesUpperCase = passwordEncoder.matches("TESTPASSWORD", encodedPassword);
        boolean matchesOriginal = passwordEncoder.matches("TestPassword", encodedPassword);

        // then
        assertFalse(matchesLowerCase, "Password matching should be case-sensitive");
        assertFalse(matchesUpperCase, "Password matching should be case-sensitive");
        assertTrue(matchesOriginal, "Original password should match");
    }
}
