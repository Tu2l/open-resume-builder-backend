package com.tu2l.gateway.filter;

import com.tu2l.common.constant.CommonConstants;
import com.tu2l.common.constant.RequestType;
import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.gateway.service.AuthGatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthGatewayFilterTest {

    @Mock
    private AuthGatewayService authService;

    private AuthGatewayFilter filter;

    @BeforeEach
    void setUp() {
        filter = new AuthGatewayFilter(authService);
    }

    private MockServerWebExchange exchange(String path, RequestType requestType) {
        return MockServerWebExchange.from(
                MockServerHttpRequest.get(path)
                        .header(CommonConstants.Headers.X_REQUEST_TYPE, requestType.name())
                        .build());
    }

    private MockServerWebExchange exchange(String path, RequestType requestType, String authHeader) {
        return MockServerWebExchange.from(
                MockServerHttpRequest.get(path)
                        .header(CommonConstants.Headers.X_REQUEST_TYPE, requestType.name())
                        .header("Authorization", authHeader)
                        .build());
    }

    @Test
    void missingRequestTypeHeaderThrows() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/users/v1/me").build());
        assertThatThrownBy(() -> filter.filter(exchange, ex -> Mono.empty()).block())
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void publicRequestPassesThroughWithoutValidation() {
        var exchange = exchange("/api/users/v1/auth/login", RequestType.PUBLIC);
        filter.filter(exchange, ex -> Mono.empty()).block();
        verify(authService, never()).validateToken(anyString());
    }

    @Test
    void protectedRequestWithoutAuthHeaderThrows() {
        var exchange = exchange("/api/users/v1/me", RequestType.PROTECTED);
        assertThatThrownBy(() -> filter.filter(exchange, ex -> Mono.empty()).block())
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Authorization");
    }

    @Test
    void protectedRequestWithInvalidTokenThrows() {
        when(authService.validateToken(anyString())).thenReturn(false);
        var exchange = exchange("/api/users/v1/me", RequestType.PROTECTED, "Bearer invalid.token");
        assertThatThrownBy(() -> filter.filter(exchange, ex -> Mono.empty()).block())
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void protectedRequestWithValidTokenPasses() throws Exception {
        when(authService.validateToken(anyString())).thenReturn(true);
        when(authService.mutateRequestWithUserInfo(any(), anyString()))
                .thenAnswer(inv -> inv.getArgument(0));
        var exchange = exchange("/api/users/v1/me", RequestType.PROTECTED, "Bearer valid.jwt.token");
        filter.filter(exchange, ex -> Mono.empty()).block();
        verify(authService).validateToken("valid.jwt.token");
    }
}
