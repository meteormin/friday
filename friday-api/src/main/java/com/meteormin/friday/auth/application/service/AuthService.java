package com.meteormin.friday.auth.application.service;

import com.meteormin.friday.auth.application.port.in.query.RetrieveUserInfoQuery;
import com.meteormin.friday.auth.application.port.in.usecase.AuthUsecase;
import com.meteormin.friday.auth.application.port.out.AuthPort;
import com.meteormin.friday.auth.domain.Auth;
import com.meteormin.friday.auth.domain.Token;
import com.meteormin.friday.common.hexagon.annotation.Usecase;
import lombok.RequiredArgsConstructor;

@Usecase
@RequiredArgsConstructor
public class AuthService implements RetrieveUserInfoQuery, AuthUsecase {
    private final AuthPort authPort;

    @Override
    public Auth retrieveUserInfo() {
        return authPort.retrieveUserInfo();
    }

    @Override
    public Token refreshToken(String token) {
        return authPort.refreshToken(token);
    }

    @Override
    public Auth signup(Auth auth) {
        return authPort.signup(auth);
    }

    @Override
    public void revokeToken() {
        authPort.revokeToken();
    }
}
