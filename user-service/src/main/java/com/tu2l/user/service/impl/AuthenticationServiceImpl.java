package com.tu2l.user.service.impl;

import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.common.model.JwtTokenType;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.model.request.RegisterRequest;
import com.tu2l.user.service.AuthenticationService;
import com.tu2l.user.service.facade.UserAuthenticationFacade;
import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserAuthenticationFacade userAuthenticationFacade;

    @Override
    public UserEntity register(RegisterRequest request) throws UserException {
        return userAuthenticationFacade.register(request);
    }

    @Override
    public UserEntity authenticate(String email, String password, boolean rememberMe) throws UserException, AuthenticationException {
        return userAuthenticationFacade.authenticate(email, password, rememberMe);
    }

    @Override
    public UserEntity refreshToken(String refreshToken, String username) throws JwtException, AuthenticationException, UserException {
        return userAuthenticationFacade.refreshToken(refreshToken, username);
    }

    @Override
    public boolean logout(String token) throws JwtException, AuthenticationException {
        return userAuthenticationFacade.invalidateToken(token);
    }

    @Override
    public boolean forgotPassword(String email) throws JwtException, AuthenticationException {
        return userAuthenticationFacade.forgotPassword(email);
    }

    @Override
    public boolean resetPassword(String passwordResetToken, String newPassword) throws JwtException, UserException, AuthenticationException {
        return userAuthenticationFacade.resetPassword(passwordResetToken, newPassword);
    }

    @Override
    public boolean verifyEmail(String verificationToken) throws JwtException, AuthenticationException {
        return userAuthenticationFacade.validateToken(verificationToken, JwtTokenType.EMAIL_VERIFICATION);
    }
}
