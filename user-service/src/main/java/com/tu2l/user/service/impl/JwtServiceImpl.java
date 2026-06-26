package com.tu2l.user.service.impl;

import com.tu2l.common.model.JwtTokenType;
import com.tu2l.common.model.states.UserRole;
import com.tu2l.common.util.JwtUtil;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.service.AuthTokenService;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class JwtServiceImpl implements AuthTokenService {
    private final JwtUtil jwtUtil;

    public JwtServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public String generateToken(UserEntity user, JwtTokenType tokenType) throws JwtException {
        return switch (tokenType) {
            case ACCESS -> jwtUtil.generateAccessToken(user.getUsername(), user.getEmail(), user.getRole().name());
            case REFRESH -> jwtUtil.generateRefreshToken(user.getUsername());
            case PASSWORD_RESET -> jwtUtil.generatePasswordResetToken(user.getUsername(), user.getEmail());
            case EMAIL_VERIFICATION -> jwtUtil.generateEmailVerificationToken(user.getUsername(), user.getEmail());
        };
    }

    @Override
    public boolean validateToken(String token, JwtTokenType tokenType) {
        // A token is valid only if it is unexpired AND was issued for the expected
        // purpose, so e.g. an access token cannot be used to reset a password.
        // An expired/malformed/wrong-signature token is simply invalid here — callers
        // (resetPassword/verifyEmail) expect a friendly false rather than a thrown 401.
        try {
            return !jwtUtil.isTokenExpired(token) && getTokenType(token) == tokenType;
        } catch (JwtException e) {
            return false;
        }
    }

    @Override
    public String refreshAccessToken(String refreshToken, UserEntity user) throws JwtException {
        if (!jwtUtil.validateRefreshToken(refreshToken, user.getUsername())) {
            throw new JwtException("Invalid refresh token");
        }
        return generateToken(user, JwtTokenType.ACCESS);
    }

    @Override
    public String getUsername(String token) {
        return jwtUtil.extractUsername(token);
    }

    @Override
    public boolean verifyRole(String token, UserRole role) {
        return jwtUtil.extractRole(token).equals(role.name());
    }

    @Override
    public long getTokenRemainingTime(String token) {
        return jwtUtil.getTokenRemainingTime(token);
    }

    @Override
    public LocalDateTime issuedAt(String token) {
        return jwtUtil.issuedAt(token);
    }

    @Override
    public LocalDateTime expiresAt(String token) {
        return jwtUtil.expiresAt(token);
    }

    @Override
    public JwtTokenType getTokenType(String token) {
        return JwtTokenType.fromValue(jwtUtil.extractTokenType(token));
    }
}
