package com.miniyus.friday.unit.infrastructure.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Date;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.miniyus.friday.infrastructure.jwt.JwtProvider;

/**
 * Jwt Provider Test
 * 
 * @author miniyus
 * @date 2023/09/04
 */
@ExtendWith(MockitoExtension.class)
public class JwtProviderTest {

    private JwtProvider jwtProvider;

    private final Faker faker = new Faker();

    @BeforeEach
    void setJwtProvider() {
        jwtProvider = new JwtProvider(
                "test-secret",
                3600L,
                86400L,
                "Authorization",
                "RefreshToken");
    }

    @Test
    void createAccessTokenTest() {
        String testEmail = faker.internet().safeEmailAddress();
        String token = jwtProvider.createAccessToken(testEmail);

        assertNotNull(token, "failed to create access token");
        assertTrue(jwtProvider.isTokenValid(token), "failed to validate access token");
    }

    /**
     * Test case for the createRefreshToken method.
     *
     * @return void
     */
    @Test
    void createRefreshTokenTest() {
        String token = jwtProvider.createRefreshToken();
        assertNotNull(token, "failed to create refresh token");
        assertTrue(jwtProvider.isTokenValid(token), "failed to validate refresh token");
    }

    /**
     * Test case for the `isTokenValid` function.
     *
     */
    @Test
    void isTokenValidTest() {
        String token = "I'm a Token";
        assertFalse(jwtProvider.isTokenValid(token), "failed to validate invalid token, this is not a token");
    }

    /**
     * Test case for the `extractTokenExpires` function.
     *
     * This test case verifies that the `extractTokenExpires` function correctly
     * extracts the expiration date from a JWT token and ensures that the
     * extracted date is not null and is in the future.
     *
     */
    @Test
    void extractTokenExpiresTest() {
        String email = faker.internet().safeEmailAddress();
        String token = jwtProvider.createAccessToken(email);
        Date expDate = jwtProvider.extractExpiresAt(token).orElse(null);
        assertNotNull(expDate);
        assertTrue(expDate.getTime() > new Date().getTime());
    }

    /**
     * Test case for the extractEmail method.
     *
     * This test case verifies the behavior of the extractEmail method.
     * It checks if the extracted email from the token matches the expected email.
     */
    @Test
    void extractEmailTest() {
        String email = faker.internet().safeEmailAddress();
        String token = jwtProvider.createAccessToken(email);
        String extractEmail = jwtProvider.extractEmail(token).orElse(null);
        assertNotNull(extractEmail);
        assertEquals(extractEmail, email);
    }
}
