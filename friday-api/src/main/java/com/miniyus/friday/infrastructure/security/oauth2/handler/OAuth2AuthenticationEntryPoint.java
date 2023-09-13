package com.miniyus.friday.infrastructure.security.oauth2.handler;

import java.io.IOException;

import com.miniyus.friday.infrastructure.security.AuthResponseHandler;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.miniyus.friday.common.error.AuthErrorCode;
import com.miniyus.friday.infrastructure.advice.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * [description]
 *
 * @author miniyus
 * @date 2023/09/01
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final MessageSource messageSource;
    private final AuthResponseHandler responseHandler;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        log.debug("Authentication Exception: {}", exception.getMessage());

        var code = AuthErrorCode.ACCESS_DENIED;
        var message = messageSource.getMessage("error.accessDenied",
            null,
            exception.getLocalizedMessage(),
            LocaleContextHolder.getLocale());

        responseHandler.handleErrorResponse(
            response,
            code,
            message);
    }

}
