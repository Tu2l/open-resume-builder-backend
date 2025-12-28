package com.tu2l.user.service.impl;

import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.exception.UserException;
import com.tu2l.user.model.request.RegisterRequest;
import com.tu2l.user.service.AuthenticationService;
import com.tu2l.user.service.helper.UserAuthenticationFacade;
import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@AllArgsConstructor
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
    public UserEntity authenticate(String usernameOrEmail, String password, boolean rememberMe) throws UserException, AuthenticationException {
        return userAuthenticationFacade.authenticate(usernameOrEmail, password, rememberMe);
    }


    @Override
    public UserEntity refreshToken(String refreshToken, String username) throws JwtException, AuthenticationException, UserException {
        return userAuthenticationFacade.refreshToken(refreshToken, username);
    }

    @Override
    public void logout(String token) throws JwtException, AuthenticationException {

    }

    @Override
    public UserEntity forgotPassword(String email) throws JwtException, AuthenticationException {
        return null;
    }

    @Override
    public UserEntity resetPassword(String passwordResetToken, String newPassword) throws JwtException, UserException, AuthenticationException {
        return null;
    }

    @Override
    public UserEntity verifyEmail(String verificationToken) throws JwtException, AuthenticationException {
        return null;
    }
}
