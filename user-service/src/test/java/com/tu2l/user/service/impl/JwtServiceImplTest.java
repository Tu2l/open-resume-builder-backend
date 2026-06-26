package com.tu2l.user.service.impl;

import com.tu2l.common.model.JwtTokenType;
import com.tu2l.common.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @Mock
    JwtUtil jwtUtil;

    @InjectMocks
    JwtServiceImpl jwtService;

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
}
