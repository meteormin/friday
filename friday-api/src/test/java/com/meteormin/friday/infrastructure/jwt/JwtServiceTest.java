package com.meteormin.friday.infrastructure.jwt;

import com.meteormin.friday.infrastructure.persistence.entities.AccessTokenEntity;
import com.meteormin.friday.infrastructure.persistence.entities.RefreshTokenEntity;
import com.meteormin.friday.infrastructure.persistence.entities.UserEntity;
import com.meteormin.friday.infrastructure.persistence.repositories.AccessTokenEntityRepository;
import com.meteormin.friday.infrastructure.persistence.repositories.RefreshTokenEntityRepository;
import com.meteormin.friday.infrastructure.persistence.repositories.UserEntityRepository;
import com.meteormin.friday.infrastructure.security.social.SocialProvider;
import com.meteormin.friday.users.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * JWT Service Test
 *
 * @author meteormin
 * @date 2023/09/06
 */
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private UserEntityRepository userRepository;

    @Mock
    private AccessTokenEntityRepository accessTokenRepository;

    @Mock
    private RefreshTokenEntityRepository refreshTokenRepository;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private JwtService jwtService;

    private UserEntity testUser;

    /**
     * Set up the test environment before each test case.
     */
    @BeforeEach
    void setUp() {
        // jwt provider dependency injection is not auto-injected to @Mockannotaion,
        // so manual injection
        jwtProvider = new JwtProvider(
                "test-secret",
                3600L,
                86400L,
                "Authorization",
                "RefreshToken");

        jwtService = new JwtService(
                jwtProvider,
                userRepository,
                accessTokenRepository,
                refreshTokenRepository);

        testUser = UserEntity.builder()
                .name("meteormin")
                .password("test@password")
                .email("meteormin@gmail.com")
                .role(UserRole.USER)
                .provider(SocialProvider.NONE)
                .build();
    }

    /**
     * A test for the issueToken method.
     */
    @Test
    void issueTokenTest() {

        var userEntity = UserEntity.create(
                testUser.getSnsId(),
                testUser.getProvider(),
                testUser.getEmail(),
                testUser.getPassword(),
                testUser.getName(),
                testUser.getRole());
        // If user repository calls findByEmail, specify the return value as arbitrary test data
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(
                Optional.of(userEntity.setId(
                        1L)));

        var testAccessToken = AccessTokenEntity.builder()
                .token(jwtProvider.createAccessToken(testUser.getEmail()))
                .expiration(3600L)
                .type(JwtProvider.BEARER)
                .userId("1")
                .build();

        when(accessTokenRepository.save(any())).thenReturn(
                AccessTokenEntity.builder()
                        .id("1")
                        .userId("1")
                        .type(JwtProvider.BEARER)
                        .token(testAccessToken.getToken())
                        .userId(testAccessToken.getUserId())
                        .expiration(testAccessToken.getExpiration())
                        .build());

        var testRefreshToken = RefreshTokenEntity.builder()
                .token(jwtProvider.createRefreshToken())
                .expiration(3600L)
                .type(JwtProvider.BEARER)
                .accessTokenId("1")
                .userId("1")
                .build();

        when(refreshTokenRepository.save(any())).thenReturn(
                new RefreshTokenEntity(
                        "1",
                        JwtProvider.BEARER,
                        testRefreshToken.getToken(),
                        testRefreshToken.getAccessTokenId(),
                        testRefreshToken.getUserId(),
                        testRefreshToken.getExpiration()));

        var tokens = jwtService.issueToken(testUser.getEmail());
        var accessToken = tokens.accessToken();
        var refreshToken = tokens.refreshToken();

        // just null check
        assertThat(tokens)
                .isNotNull()
                .hasFieldOrProperty("tokenType")
                .hasFieldOrProperty("accessToken")
                .hasFieldOrProperty("expiresIn")
                .hasFieldOrProperty("refreshToken");

        // jwt token validations
        assertThat(jwtProvider.isTokenValid(accessToken)).isTrue();
        assertThat(jwtProvider.isTokenValid(refreshToken)).isTrue();
        var extractEmail = jwtProvider.extractEmail(accessToken).orElse(null);
        // check extracted email from access token
        assertThat(extractEmail)
                .isNotNull()
                .isEqualTo(testUser.getEmail());

        /*
         * Verify that the expiration date of the access token. check injected into the JWT Provider
         * matches the expiration date of the issued access token
         */
        assertThat(jwtProvider.extractExpiresAt(accessToken))
                .isNotNull()
                .isEqualTo(jwtProvider.extractExpiresAt(testAccessToken.getToken()));
    }
}
