package com.tu2l.user.service.impl;

import com.tu2l.common.model.JwtTokenType;
import com.tu2l.common.model.states.UserRole;
import com.tu2l.common.util.JwtUtil;
import com.tu2l.user.config.AuthConfigValues;
import com.tu2l.user.entity.UserEntity;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @Mock
    JwtUtil jwtUtil;

    // 60-minute remember-me window distinguishes it from the default access validity.
    private static final AuthConfigValues CFG = new AuthConfigValues(5, 15, 60, false);

    JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl(jwtUtil, CFG);
    }

    @Test
    void validateToken_returnsFalseWhenTokenExpired_insteadOfThrowing() {
        // jjwt throws ExpiredJwtException (a JwtException) while parsing an expired token.
        when(jwtUtil.isTokenExpired("expired")).thenThrow(new JwtException("expired"));

        assertThat(jwtService.validateToken("expired", JwtTokenType.PASSWORD_RESET)).isFalse();
    }

    @Test
    void validateToken_falseWhenTokenTypeMismatch() {
        when(jwtUtil.isTokenExpired("tok")).thenReturn(false);
        when(jwtUtil.extractTokenType("tok")).thenReturn(JwtTokenType.ACCESS.getValue());

        assertThat(jwtService.validateToken("tok", JwtTokenType.PASSWORD_RESET)).isFalse();
    }

    @Test
    void validateToken_trueWhenUnexpiredAndTypeMatches() {
        when(jwtUtil.isTokenExpired("tok")).thenReturn(false);
        when(jwtUtil.extractTokenType("tok")).thenReturn(JwtTokenType.PASSWORD_RESET.getValue());

        assertThat(jwtService.validateToken("tok", JwtTokenType.PASSWORD_RESET)).isTrue();
    }

    @Test
    void generateAccessToken_default_usesDefaultValidity() {
        var user = userWith();
        when(jwtUtil.generateAccessToken("john", "john@example.com", "USER")).thenReturn("access");

        assertThat(jwtService.generateAccessToken(user, false)).isEqualTo("access");
    }

    @Test
    void generateAccessToken_rememberMe_usesRememberMeValidity() {
        var user = userWith();
        when(jwtUtil.generateAccessToken("john", "john@example.com", "USER", 60L)).thenReturn("rememberAccess");

        assertThat(jwtService.generateAccessToken(user, true)).isEqualTo("rememberAccess");
    }

    private UserEntity userWith() {
        return UserEntity.builder()
                .username("john")
                .email("john@example.com")
                .role(UserRole.USER)
                .build();
    }
}
