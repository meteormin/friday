package com.miniyus.friday.infrastructure.security.oauth2.handler;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.miniyus.friday.infrastructure.jwt.IssueToken;
import com.miniyus.friday.infrastructure.jwt.JwtService;
import com.miniyus.friday.infrastructure.security.PrincipalUserInfo;
import com.miniyus.friday.infrastructure.security.oauth2.response.OAuth2TokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * [description]
 *
 * @author seongminyoo
 * @date 2023/08/31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        log.debug("OAuth2 Login 성공!");

        if (response.isCommitted()) {
            log.debug("Response has already been committed.");
            return;
        }

        try {
            PrincipalUserInfo oAuth2User = (PrincipalUserInfo) authentication.getPrincipal();

            issueToken(response, oAuth2User);
        } catch (Exception e) {
            throw e;
        }
    }

    private void issueToken(HttpServletResponse response, PrincipalUserInfo oAuth2User)
            throws IOException {
        IssueToken issueToken = jwtService.issueToken(oAuth2User.getId());
        OAuth2TokenResponse oAuth2TokenResponse = new OAuth2TokenResponse(
                oAuth2User.getId(),
                oAuth2User.getSnsId(),
                oAuth2User.getProvider().getId(),
                issueToken);

        String jsonBody = objectMapper.writeValueAsString(oAuth2TokenResponse);
        response.setHeader("Content-Type", "application/json");
        response.setStatus(HttpStatus.CREATED.value());
        response.getWriter().write(jsonBody);
    }
}