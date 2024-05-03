package com.meteormin.friday.infrastructure.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Optional;

/**
 * [description]
 *
 * @author seongminyoo
 * @since 2023/08/31
 */
@Slf4j
public record JwtProvider(String secret, Long accessTokenExpiration, Long refreshTokenExpiration,
        String accessTokenKey, String refreshTokenKey) {

    public static final String ACCESS_TOKEN_SUBJECT = "accessToken";
    public static final String REFRESH_TOKEN_SUBJECT = "refreshToken";
    public static final String EMAIL_CLAIM = "email";
    public static final String BEARER = "Bearer";

    public String createAccessToken(String email) {
        Date now = new Date();
        return JWT.create()
                .withSubject(ACCESS_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + (accessTokenExpiration * 1000))) // 토큰 만료 시간
                // 설정
                // 클레임으로는 저희는 email 하나만 사용합니다.
                // 추가적으로 식별자나, 이름 등의 정보를 더 추가하셔도 됩니다.
                // 추가하실 경우 .withClaim(클래임 이름, 클래임 값) 으로 설정해주시면 됩니다
                .withClaim(EMAIL_CLAIM, email).sign(Algorithm.HMAC512(secret)); // HMAC512 알고리즘 사용,
        // application-jwt.yml에서
        // 지정한 secret 키로 암호화
    }

    /**
     * RefreshToken 생성 RefreshToken은 Claim에 email도 넣지 않으므로 withClaim() X
     */
    public String createRefreshToken() {
        Date now = new Date();
        return JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + (refreshTokenExpiration * 1000)))
                .sign(Algorithm.HMAC512(secret));
    }

    /**
     * 헤더에서 AccessToken 추출 토큰 형식 : Bearer XXX에서 Bearer를 제외하고 순수 토큰만 가져오기 위해서 헤더를 가져온 후 "Bearer"를
     * 삭제(""로 replace)
     */
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessTokenKey))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken
                        .replace(BEARER, "")
                        .trim());
    }

    /**
     * AccessToken에서 Email 추출 추출 전에 JWT.require()로 검증기 생성 verify로 AceessToken 검증 후 유효하다면
     * getClaim()으로 이메일 추출 유효하지 않다면 빈 Optional 객체 반환
     */
    public Optional<String> extractEmail(String accessToken) {
        try {
            // 토큰 유효성 검사하는 데에 사용할 알고리즘이 있는 JWT verifier builder 반환
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secret)).build() // 반환된 빌더로 JWT
                    // verifier 생성
                    .verify(accessToken) // accessToken을 검증하고 유효하지 않다면 예외 발생
                    .getClaim(EMAIL_CLAIM) // claim(Emial) 가져오기
                    .asString());
        } catch (Exception e) {
            log.error("Failed extract email, invalid JWT Token");
            return Optional.empty();
        }
    }

    public Optional<Date> extractExpiresAt(String accessToken) {
        try {
            // 토큰 유효성 검사하는 데에 사용할 알고리즘이 있는 JWT verifier builder 반환
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secret)).build() // 반환된 빌더로 JWT
                    // verifier 생성
                    .verify(accessToken) // accessToken을 검증하고 유효하지 않다면 예외 발생
                    .getExpiresAt());
        } catch (Exception e) {
            log.error("Failed extract expiresAt, invalid JWT Token");
            return Optional.empty();
        }
    }

    public boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secret)).build().verify(token);
            return true;
        } catch (Exception e) {
            log.error("invalid token: {}", e.getMessage());
            return false;
        }
    }
}
