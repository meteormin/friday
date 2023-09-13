package com.miniyus.friday.infrastructure.security.auth.handler;

import java.io.IOException;

import com.miniyus.friday.infrastructure.security.AuthResponseHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniyus.friday.infrastructure.jwt.IssueToken;
import com.miniyus.friday.infrastructure.jwt.JwtService;
import com.miniyus.friday.infrastructure.security.PrincipalUserInfo;
import com.miniyus.friday.infrastructure.security.auth.response.PasswordTokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * [description]
 *
 * @author seongminyoo
 * @date 2023/09/04
 */
@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Value("${jwt.access.expiration}")
    private String accessTokenExpiration;

    private final JwtService jwtService;
    private final AuthResponseHandler responseHandler;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        var userDetails = (PrincipalUserInfo) authentication.getPrincipal();

        IssueToken tokens = jwtService.issueToken(userDetails.getId());

        log.debug("Success Login Email: {}", userDetails.getUsername());
        log.debug("Success Login AccessToken : {}", tokens.accessToken());
        log.debug("Issued AccessToken expires in: {}(seconds)", tokens.expiresIn());

        responseHandler.handlePasswordIssueTokenResponse(
            response,
            userDetails,
            tokens
        );
    }
}
