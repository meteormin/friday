package com.meteormin.friday.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteormin.friday.common.error.ErrorCode;
import com.meteormin.friday.common.error.ErrorResponse;
import com.meteormin.friday.infrastructure.jwt.IssueToken;
import com.meteormin.friday.infrastructure.persistence.entities.LoginHistoryEntity;
import com.meteormin.friday.infrastructure.persistence.entities.UserEntity;
import com.meteormin.friday.infrastructure.persistence.repositories.LoginHistoryEntityRepository;
import com.meteormin.friday.infrastructure.persistence.repositories.UserEntityRepository;
import com.meteormin.friday.infrastructure.security.auth.response.PasswordTokenResponse;
import com.meteormin.friday.infrastructure.security.social.response.OAuth2TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * AuthResponseHandler
 *
 * @author meteormin
 * @since 2023/09/13
 */
@Component
@RequiredArgsConstructor
public class AuthResponseHandler {
    /**
     * messageSource
     */
    private final MessageSource messageSource;

    /**
     * objectMapper
     */
    private final ObjectMapper objectMapper;

    /**
     * userEntityRepository
     */
    private final UserEntityRepository userEntityRepository;

    /**
     * loginHistoryEntityRepository
     */
    private final LoginHistoryEntityRepository loginHistoryEntityRepository;

    /**
     * A method to handle an error response.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param errorCode the error code
     * @param message the error message
     * @throws IOException if an I/O error occurs
     */
    public void handleErrorResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            ErrorCode errorCode,
            String message) throws IOException {
        String translateMessage = messageSource
                .getMessage(message, null, message, LocaleContextHolder.getLocale());
        var errorResponse = new ErrorResponse(errorCode, translateMessage);
        handleJsonResponse(response, errorCode.getHttpStatus(), errorResponse);
    }

    /**
     * Handle the response from the OAuth2 token issuance.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param userInfo the principal user information
     * @param token the issued token
     * @throws IOException if there is an I/O error while handling the response
     */
    public void handleOAuth2IssueTokenResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            PrincipalUserInfo userInfo,
            IssueToken token) throws IOException {

        OAuth2TokenResponse tokenResponse = new OAuth2TokenResponse(
                userInfo.getId(),
                userInfo.getSnsId(),
                userInfo.getProvider().value(),
                userInfo.getEmail(),
                token);

        handleLoginHistory(request, userInfo.getId());
        handleJsonResponse(response, HttpStatus.CREATED, tokenResponse);
    }

    /**
     * Handle the OAuth2 issue token header.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param userInfo the principal user info
     * @param token the issue token
     */
    public void handleOAuth2IssueTokenHeader(
            HttpServletRequest request,
            HttpServletResponse response,
            PrincipalUserInfo userInfo,
            IssueToken token) {
        handleLoginHistory(request, userInfo.getId());
        response.setStatus(HttpStatus.CREATED.value());
        response.setHeader(token.accessTokenKey(), "Bearer " + token.accessToken());
        response.setHeader(token.refreshTokenKey(), "Bearer " + token.refreshToken());
    }

    /**
     * Handles the response for issuing a password token.
     *
     * @param request the HTTP request object
     * @param response the HTTP response object
     * @param userInfo the principal user information
     * @param token the issued token
     * @throws IOException if there is an I/O error
     */
    public void handlePasswordIssueTokenResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            PrincipalUserInfo userInfo,
            IssueToken token) throws IOException {

        PasswordTokenResponse tokenResponse = new PasswordTokenResponse(
                userInfo.getId(),
                userInfo.getUsername(),
                userInfo.getName(),
                token);

        handleLoginHistory(request, userInfo.getId());
        handleJsonResponse(response, HttpStatus.CREATED, tokenResponse);
    }

    /**
     * Handles the JSON response for the given HTTP response, HTTP status, and response body.
     *
     * @param response the HTTP response object
     * @param status the HTTP status code
     * @param body the response body object
     * @throws IOException if there is an error writing the JSON body to the response writer
     */
    private void handleJsonResponse(
            HttpServletResponse response,
            HttpStatus status,
            Object body) throws IOException {

        response.setHeader("Content-Type", "application/json");
        response.setStatus(status.value());
        String jsonBody = objectMapper.writeValueAsString(body);
        response.getWriter().write(jsonBody);
    }

    /**
     * Handles the login history for a user.
     *
     * @param request the HttpServletRequest object containing the login history information
     * @param userId the ID of the user
     */
    private void handleLoginHistory(
            HttpServletRequest request,
            Long userId) {

        LoginHistoryEntity loginHistoryEntity = createLoginHistoryFromRequest(request);
        UserEntity user = userEntityRepository.findById(userId)
                .orElse(null);

        loginHistoryEntity.setUser(user);
        loginHistoryEntityRepository.save(loginHistoryEntity);
    }

    /**
     * Creates a LoginHistoryEntity object from the provided HttpServletRequest.
     *
     * @param request the HttpServletRequest object containing the request information
     * @return the created LoginHistoryEntity object
     */
    private LoginHistoryEntity createLoginHistoryFromRequest(HttpServletRequest request) {
        String ip;
        if (request.getHeader("X-Forwarded-For") == null) {
            ip = request.getRemoteAddr();
        } else {
            ip = request.getHeader("X-Forwarded-For");
        }

        return LoginHistoryEntity.create(null, ip);
    }
}
