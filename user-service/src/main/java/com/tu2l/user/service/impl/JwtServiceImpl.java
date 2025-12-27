package com.tu2l.user.service.impl;

import com.tu2l.common.model.JwtTokenType;
import com.tu2l.common.model.states.UserRole;
import com.tu2l.common.util.JwtUtil;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.service.AuthTokenService;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;

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
    public boolean validateToken(String token, JwtTokenType tokenType) throws JwtException {
        // TODO: Differentiate validation based on token type if needed
        return !jwtUtil.isTokenExpired(token);
    }

    @Override
    public String refreshAccessToken(String refreshToken, String username, String email, UserRole role) throws JwtException {
        if (jwtUtil.validateToken(refreshToken, username)) {
            return jwtUtil.generateAccessToken(username, email, role.name());
        } else {
            throw new JwtException("Invalid refresh token");
        }
    }

    @Override
    public String getUsername(String token) {
        return jwtUtil.extractUsername(token);
    }

    @Override
    public boolean verifyRole(String token, UserRole role) {
        return jwtUtil.extractRole(token).equals(role.name());
    }
}
