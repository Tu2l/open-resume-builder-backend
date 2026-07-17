package com.tu2l.gateway.service;

import com.tu2l.common.constant.CommonConstants;
import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.common.util.JwtUtil;
import com.tu2l.gateway.service.impl.AuthGatewayServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.reactive.ServerHttpRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthGatewayServiceImplTest {

    @Mock
    private JwtUtil jwtUtil;

    private AuthGatewayServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthGatewayServiceImpl(jwtUtil);
    }

    @Test
    void validateTokenReturnsTrueWhenNotExpired() {
        when(jwtUtil.isTokenExpired(anyString())).thenReturn(false);
        assertThat(service.validateToken("valid.token")).isTrue();
    }

    @Test
    void validateTokenReturnsFalseWhenExpired() {
        when(jwtUtil.isTokenExpired(anyString())).thenReturn(true);
        assertThat(service.validateToken("expired.token")).isFalse();
    }

    @Test
    void validateTokenPropagatesJwtException() {
        when(jwtUtil.isTokenExpired(anyString())).thenThrow(new ExpiredJwtException(null, null, "expired"));
        assertThatThrownBy(() -> service.validateToken("bad.token"))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void mutateRequestAddsUserHeaders() throws AuthenticationException {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("john");
        when(jwtUtil.extractAllClaims(anyString())).thenReturn(claims);
        when(jwtUtil.extractClaim(any(Claims.class), any()))
                .thenReturn(CommonConstants.Token.TOKEN_TYPE_BEARER)
                .thenReturn("john@example.com")
                .thenReturn("USER");

        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpRequest.Builder builder = mock(ServerHttpRequest.Builder.class);
        when(request.mutate()).thenReturn(builder);
        when(builder.header(anyString(), anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(request);

        ServerHttpRequest result = service.mutateRequestWithUserInfo(request, "some.token");
        assertThat(result).isNotNull();
    }

    @Test
    void mutateRequestThrowsWhenEmailMissing() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("john");
        when(jwtUtil.extractAllClaims(anyString())).thenReturn(claims);
        when(jwtUtil.extractClaim(any(Claims.class), any()))
                .thenReturn(CommonConstants.Token.TOKEN_TYPE_BEARER)
                .thenReturn(null)
                .thenReturn("USER");

        assertThatThrownBy(() -> service.mutateRequestWithUserInfo(mock(ServerHttpRequest.class), "bad.token"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("missing user information");
    }

    @Test
    void mutateRequestThrowsWhenTokenTypeWrong() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("john");
        when(jwtUtil.extractAllClaims(anyString())).thenReturn(claims);
        when(jwtUtil.extractClaim(any(Claims.class), any()))
                .thenReturn("refresh")
                .thenReturn("john@example.com")
                .thenReturn("USER");

        assertThatThrownBy(() -> service.mutateRequestWithUserInfo(mock(ServerHttpRequest.class), "refresh.token"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("missing user information");
    }
}
