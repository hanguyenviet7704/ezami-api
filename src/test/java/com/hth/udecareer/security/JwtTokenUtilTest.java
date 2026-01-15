package com.hth.udecareer.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtTokenUtil.
 * Tests token generation, validation, and claim extraction.
 */
@ExtendWith(MockitoExtension.class)
class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;

    private static final String TEST_SECRET = "test-secret-key-for-jwt-token-generation-must-be-long-enough";
    private static final String TEST_USERNAME = "testuser@example.com";

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil();
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", TEST_SECRET);
    }

    private UserDetails createTestUserDetails() {
        return new User(TEST_USERNAME, "password", Collections.emptyList());
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        // given
        UserDetails userDetails = createTestUserDetails();

        // when
        String token = jwtTokenUtil.generateToken(userDetails);

        // then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_shouldContainThreeParts() {
        // given
        UserDetails userDetails = createTestUserDetails();

        // when
        String token = jwtTokenUtil.generateToken(userDetails);

        // then - JWT has 3 parts separated by dots
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have header.payload.signature format");
    }

    @Test
    void getUsernameFromToken_shouldReturnCorrectUsername() {
        // given
        UserDetails userDetails = createTestUserDetails();
        String token = jwtTokenUtil.generateToken(userDetails);

        // when
        String extractedUsername = jwtTokenUtil.getUsernameFromToken(token);

        // then
        assertEquals(TEST_USERNAME, extractedUsername);
    }

    @Test
    void getExpirationDateFromToken_shouldReturnFutureDate() {
        // given
        UserDetails userDetails = createTestUserDetails();
        String token = jwtTokenUtil.generateToken(userDetails);

        // when
        Date expirationDate = jwtTokenUtil.getExpirationDateFromToken(token);

        // then
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()), "Expiration should be in the future");
    }

    @Test
    void getExpirationDateFromToken_shouldBeApproximately7DaysInFuture() {
        // given
        UserDetails userDetails = createTestUserDetails();
        String token = jwtTokenUtil.generateToken(userDetails);

        // when
        Date expirationDate = jwtTokenUtil.getExpirationDateFromToken(token);

        // then
        long expectedExpirationMs = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L);
        long actualExpirationMs = expirationDate.getTime();

        // Allow 10 seconds tolerance for test execution time
        long tolerance = 10_000L;
        assertTrue(Math.abs(expectedExpirationMs - actualExpirationMs) < tolerance,
                "Expiration should be approximately 7 days from now");
    }

    @Test
    void validateToken_withValidToken_shouldReturnTrue() {
        // given
        UserDetails userDetails = createTestUserDetails();
        String token = jwtTokenUtil.generateToken(userDetails);

        // when
        Boolean isValid = jwtTokenUtil.validateToken(token, userDetails);

        // then
        assertTrue(isValid);
    }

    @Test
    void validateToken_withDifferentUser_shouldReturnFalse() {
        // given
        UserDetails userDetails = createTestUserDetails();
        UserDetails differentUser = new User("different@example.com", "password", Collections.emptyList());
        String token = jwtTokenUtil.generateToken(userDetails);

        // when
        Boolean isValid = jwtTokenUtil.validateToken(token, differentUser);

        // then
        assertFalse(isValid);
    }

    @Test
    void validateToken_withInvalidToken_shouldThrowException() {
        // given
        UserDetails userDetails = createTestUserDetails();
        String invalidToken = "invalid.token.here";

        // when & then
        assertThrows(Exception.class, () -> jwtTokenUtil.validateToken(invalidToken, userDetails));
    }

    @Test
    void validateToken_withTamperedToken_shouldThrowException() {
        // given
        UserDetails userDetails = createTestUserDetails();
        String token = jwtTokenUtil.generateToken(userDetails);
        // Tamper with the token by changing a character in the signature
        String tamperedToken = token.substring(0, token.length() - 1) + "X";

        // when & then
        assertThrows(Exception.class, () -> jwtTokenUtil.validateToken(tamperedToken, userDetails));
    }

    @Test
    void generateToken_withShortSecret_shouldStillWork() {
        // given - test backwards compatibility with short secrets
        JwtTokenUtil utilWithShortSecret = new JwtTokenUtil();
        ReflectionTestUtils.setField(utilWithShortSecret, "secret", "short");
        UserDetails userDetails = createTestUserDetails();

        // when
        String token = utilWithShortSecret.generateToken(userDetails);

        // then
        assertNotNull(token);
        String username = utilWithShortSecret.getUsernameFromToken(token);
        assertEquals(TEST_USERNAME, username);
    }

    @Test
    void multipleTokensForSameUser_shouldAllBeValid() throws InterruptedException {
        // given
        UserDetails userDetails = createTestUserDetails();

        // when
        String token1 = jwtTokenUtil.generateToken(userDetails);
        Thread.sleep(10); // Small delay to ensure different issuedAt timestamp
        String token2 = jwtTokenUtil.generateToken(userDetails);

        // then - both tokens should be valid
        assertTrue(jwtTokenUtil.validateToken(token1, userDetails));
        assertTrue(jwtTokenUtil.validateToken(token2, userDetails));
    }

    @Test
    void tokenValidityConstant_shouldBe7Days() {
        // Verify the constant is set correctly
        assertEquals(7 * 24 * 60 * 60, JwtTokenUtil.JWT_TOKEN_VALIDITY,
                "JWT_TOKEN_VALIDITY should be 7 days in seconds");
    }
}
