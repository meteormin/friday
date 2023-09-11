package com.miniyus.friday.infrastructure.jwt;

import java.io.IOException;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniyus.friday.infrastructure.auth.PrincipalUserInfo;
import com.miniyus.friday.infrastructure.jpa.entities.UserEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JwtAuthenticationFilter
 *
 * @author seongminyoo
 * @date 2023/09/04
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String NO_CHECK_URL = "/login";
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    /**
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getRequestURI().equals(NO_CHECK_URL)) {
            filterChain.doFilter(request, response); // "/login" 요청이 들어오면, 다음 필터 호출
            return; // return으로 이후 현재 필터 진행 막기 (안해주면 아래로 내려가서 계속 필터 진행시킴)
        }

        // 사용자 요청 헤더에서 RefreshToken 추출
        // -> RefreshToken이 없거나 유효하지 않다면(DB에 저장된 RefreshToken과 다르다면) null을 반환
        // 사용자의 요청 헤더에 RefreshToken이 있는 경우는, AccessToken이 만료되어 요청한 경우밖에 없다.
        // 따라서, 위의 경우를 제외하면 추출한 refreshToken은 모두 null
        String refreshToken = jwtService.extractRefreshToken(request)
                .filter(jwtService::isTokenValid)
                .orElse(null);

        // 리프레시 토큰이 요청 헤더에 존재했다면, 사용자가 AccessToken이 만료되어서
        // RefreshToken까지 보낸 것이므로 리프레시 토큰이 DB의 리프레시 토큰과 일치하는지 판단 후,
        // 일치한다면 AccessToken을 재발급해준다.
        if (refreshToken != null) {
            checkRefreshTokenAndReIssueAccessToken(response, refreshToken);
            return; // RefreshToken을 보낸 경우에는 AccessToken을 재발급 하고 인증 처리는 하지 않게 하기위해 바로 return으로 필터 진행
                    // 막기
        }

        // RefreshToken이 없거나 유효하지 않다면, AccessToken을 검사하고 인증을 처리하는 로직 수행
        // AccessToken이 없거나 유효하지 않다면, 인증 객체가 담기지 않은 상태로 다음 필터로 넘어가기 때문에 403 에러 발생
        // AccessToken이 유효하다면, 인증 객체가 담긴 상태로 다음 필터로 넘어가기 때문에 인증 성공
        if (refreshToken == null) {
            checkAccessTokenAndAuthentication(request, response, filterChain);
        }
    }

    public void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response,
            String refreshToken)
            throws IOException {
        UserEntity user = jwtService.getUserByRefreshToken(refreshToken);
        if (user == null) {
            return;
        }

        IssueToken tokens = jwtService.issueToken(user.getId());

        String jsonBody = objectMapper.writeValueAsString(tokens);
        response.setHeader("Content-Type", "application/json");
        response.setStatus(HttpStatus.CREATED.value());
        response.getWriter().write(jsonBody);
    }

    /**
     * [액세스 토큰 체크 & 인증 처리 메소드] request에서 extractAccessToken()으로 액세스 토큰 추출 후, isTokenValid()로 유효한
     * 토큰인지 검증 유효한 토큰이면, 액세스 토큰에서 extractEmail로 Email을 추출한 후 findByEmail()로 해당 이메일을 사용하는 유저 객체 반환 그
     * 유저 객체를 saveAuthentication()으로 인증 처리하여 인증 허가 처리된 객체를 SecurityContextHolder에 담기 그 후 다음 인증 필터로
     * 진행
     */
    public void checkAccessTokenAndAuthentication(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        log.debug("checkAccessTokenAndAuthentication() 호출");
        log.debug("access token: {}", jwtService.extractAccessToken(request));

        try {
            jwtService.extractAccessToken(request)
                    .filter(jwtService::isTokenValid)
                    .ifPresent(accessToken -> jwtService.extractEmail(accessToken)
                            .ifPresent(email -> Optional
                                    .of(userDetailsService.loadUserByUsername(email))
                                    .ifPresent(this::saveAuthentication)));
        } catch (NoSuchElementException ex) {
            log.debug("error: {}", ex.getMessage());
        }

        log.debug("Next filter");

        filterChain.doFilter(request, response);
    }

    /**
     * [인증 허가 메소드] 파라미터의 유저 : 우리가 만든 회원 객체 / 빌더의 유저 : UserDetails의 User 객체
     *
     * new UsernamePasswordAuthenticationToken()로 인증 객체인 Authentication 객체 생성
     * UsernamePasswordAuthenticationToken의 파라미터 1. 위에서 만든 UserDetailsUser 객체 (유저 정보) 2.
     * credential(보통 비밀번호로, 인증 시에는 보통 null로 제거) 3. Collection < ? extends GrantedAuthority>로,
     * UserDetails의 User 객체 안에 Set<GrantedAuthority> authorities이 있어서 getter로 호출한 후에, new
     * NullAuthoritiesMapper()로 GrantedAuthoritiesMapper 객체를 생성하고 mapAuthorities()에 담기
     *
     * SecurityContextHolder.getContext()로 SecurityContext를 꺼낸 후, setAuthentication()을 이용하여 위에서 만든
     * Authentication 객체에 대한 인증 허가 처리
     */
    public void saveAuthentication(UserDetails user) {
        PrincipalUserInfo principal = (PrincipalUserInfo) user;
        log.debug("save auth: {}", user.getUsername());

        String password = principal.getPassword();
        if (password == null) {
            // 소셜 로그인 유저의 비밀번호 임의로 설정 하여 소셜 로그인 유저도 인증 되도록 설정
            long now = Instant.now().getEpochSecond();
            password = String.format("%s-%s", principal.getSnsId(), now);
        }

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, null,
                        user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
