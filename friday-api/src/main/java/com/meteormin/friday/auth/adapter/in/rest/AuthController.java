package com.meteormin.friday.auth.adapter.in.rest;

import com.meteormin.friday.api.auth.AuthApi;
import com.meteormin.friday.api.auth.resource.AuthUserResource;
import com.meteormin.friday.auth.application.port.in.query.RetrieveUserInfoQuery;
import com.meteormin.friday.auth.application.port.in.usecase.AuthUsecase;
import com.meteormin.friday.auth.domain.Auth;
import com.meteormin.friday.auth.domain.Token;
import com.meteormin.friday.common.hexagon.BaseController;
import com.meteormin.friday.common.hexagon.annotation.RestAdapter;
import com.meteormin.friday.infrastructure.config.SecurityConfiguration;
import com.meteormin.friday.infrastructure.security.auth.PasswordAuthentication;
import com.meteormin.friday.infrastructure.security.auth.response.PasswordTokenResponse;
import com.meteormin.friday.infrastructure.security.auth.userinfo.PasswordUserInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Auth Controller
 *
 * @author meteormin
 * @since 2023/09/04
 */
@RestAdapter
@RequiredArgsConstructor
public class AuthController extends BaseController implements AuthApi {
    private final RetrieveUserInfoQuery retrieveUserInfoQuery;
    private final AuthUsecase authUsecase;

    /**
     * Creates a new user account by signing up.
     *
     * @param authentication the user authentication information
     * @return the principal user info of the newly created user
     */
    @PostMapping(SecurityConfiguration.SIGNUP_URL)
    public ResponseEntity<AuthUserResource> signup(
            @Valid @RequestBody PasswordUserInfo authentication) {
        // Call the authService.signup() method to sign up the user and get the user information

        var authDomain = Auth.builder()
                .email(authentication.email())
                .name(authentication.name())
                .password(authentication.password())
                .build();

        var user = authUsecase.signup(authDomain);

        // Build the URI for the /v1/auth/me endpoint
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequestUri();
        builder.path("/v1/auth/me");
        var uri = builder.build().toUri();

        // Return a ResponseEntity with the created URI and the user information
        return ResponseEntity.created(uri).body(AuthUserResource.fromDomain(user));
    }

    @Override
    @GetMapping(SecurityConfiguration.OAUTH2_LOGIN_URL + "/{provider}")
    public void oauth2Login(@PathVariable String provider) {
        // just for documentation.
    }

    @Override
    @PostMapping(SecurityConfiguration.LOGIN_URL)
    public ResponseEntity<PasswordTokenResponse> signin(PasswordAuthentication authentication) {
        // just for documentation.
        return null;
    }

    /**
     * Refreshes the access token using the provided refresh token.
     *
     * @param refreshToken The refresh token.
     * @return The response entity containing the issued access token.
     */
    @PostMapping(SecurityConfiguration.REFRESH_URL)
    public ResponseEntity<Token> refresh(
            @RequestHeader(name = "RefreshToken") String refreshToken) {
        // Call the authService to refresh the token
        var tokens = authUsecase.refreshToken(refreshToken);
        // Create a response entity with the issued access token
        var uri = createUriFromContextPath(SecurityConfiguration.USERINFO_URL);
        return ResponseEntity
                .created(uri).body(tokens);
    }

    @GetMapping(SecurityConfiguration.USERINFO_URL)
    @PreAuthorize("hasAnyAuthority('user', 'admin')")
    public ResponseEntity<AuthUserResource> userInfo() {
        var userInfo = retrieveUserInfoQuery.retrieveUserInfo();
        return ResponseEntity.ok(AuthUserResource.fromDomain(userInfo));
    }

    @PostMapping(SecurityConfiguration.LOGOUT_URL)
    @PreAuthorize("hasAnyAuthority('user', 'admin')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeToken() {
        authUsecase.revokeToken();
    }
}
