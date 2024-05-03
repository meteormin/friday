package com.meteormin.friday.auth.adapter.out.persistence;

import com.meteormin.friday.auth.adapter.out.persistence.mapper.AuthMapper;
import com.meteormin.friday.auth.application.port.out.AuthPort;
import com.meteormin.friday.auth.domain.Auth;
import com.meteormin.friday.auth.domain.Token;
import com.meteormin.friday.common.error.RestErrorCode;
import com.meteormin.friday.common.error.RestErrorException;
import com.meteormin.friday.common.hexagon.annotation.PersistenceAdapter;
import com.meteormin.friday.infrastructure.jwt.JwtProvider;
import com.meteormin.friday.infrastructure.jwt.JwtService;
import com.meteormin.friday.infrastructure.persistence.entities.UserEntity;
import com.meteormin.friday.infrastructure.security.CustomUserDetailsService;
import com.meteormin.friday.infrastructure.security.PrincipalUserInfo;
import com.meteormin.friday.infrastructure.security.auth.userinfo.PasswordUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@PersistenceAdapter
@Transactional
public class AuthAdapter implements AuthPort {
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMapper authMapper;

    @Override
    public Auth signup(Auth authentication) {
        try {
            userDetailsService.setPasswordEncoder(passwordEncoder);
            var principalUserInfo = userDetailsService.create(
                    PasswordUserInfo.builder()
                            .email(authentication.getEmail())
                            .name(authentication.getName())
                            .password(passwordEncoder.encode(authentication.getPassword()))
                            .build());
            return authMapper.toAuthDomain(principalUserInfo);
        } catch (Exception e) {
            throw new RestErrorException("auth.error.exists", RestErrorCode.CONFLICT, e);
        }
    }

    @Override
    public Token refreshToken(String refreshToken) {
        var tokenInfo = refreshToken.split(" ");
        var tokenType = tokenInfo[0];
        var token = tokenInfo[1];

        if (!tokenType.equals(JwtProvider.BEARER)) {
            throw new RestErrorException(
                    "error.unsupportedTokenType",
                    RestErrorCode.UNSUPPORTED_TOKEN_TYPE);
        }

        UserEntity user = jwtService.getUserByRefreshToken(token).orElse(null);
        if (user == null) {
            throw new RestErrorException(
                    "error.userNotFound",
                    RestErrorCode.ACCESS_DENIED);
        }

        var issueToken = jwtService.issueToken(user.getId());
        return authMapper.toTokenDomain(issueToken);
    }

    @Override
    public Auth retrieveUserInfo() {
        var user = userInfo();
        if (user == null) {
            throw userNotExists();
        }

        return authMapper.toAuthDomain(user);
    }

    @Override
    public void revokeToken() {
        var user = userInfo();
        if (user == null) {
            throw userNotExists();
        }

        jwtService.revokeTokenByUserId(user.getId());
    }

    private PrincipalUserInfo userInfo() {
        return (PrincipalUserInfo) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private RestErrorException userNotExists() {
        return new RestErrorException(
                "auth.error.notFound",
                RestErrorCode.NOT_FOUND);
    }
}
