package com.tu2l.gateway.filter;

import com.tu2l.common.constant.CommonConstants;
import com.tu2l.common.constant.RequestType;
import com.tu2l.gateway.config.CustomGatewayProperties;
import com.tu2l.gateway.util.WebPathUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalRequestFilterTest {

    private GlobalRequestFilter filter;

    @BeforeEach
    void setUp() {
        CustomGatewayProperties props = new CustomGatewayProperties();
        props.setPublicRoutes(List.of("/api/users/v1/auth/**", "/api/pdf/**"));
        filter = new GlobalRequestFilter(props, new WebPathUtil(new AntPathMatcher()));
    }

    private ServerWebExchange exchange(String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
    }

    private ServerWebExchange exchangeWithCorrelation(String path, String correlationId) {
        return MockServerWebExchange.from(
                MockServerHttpRequest.get(path)
                        .header(CommonConstants.Headers.X_CORRELATION_ID, correlationId)
                        .build());
    }

    private AtomicReference<ServerHttpRequest> captureRequest(ServerWebExchange exchange) {
        AtomicReference<ServerHttpRequest> captured = new AtomicReference<>();
        filter.filter(exchange, ex -> {
            captured.set(ex.getRequest());
            return Mono.empty();
        }).block();
        return captured;
    }

    @Test
    void publicRouteSetsPublicRequestType() {
        var captured = captureRequest(exchange("/api/users/v1/auth/login"));
        assertThat(captured.get().getHeaders().getFirst(CommonConstants.Headers.X_REQUEST_TYPE))
                .isEqualTo(RequestType.PUBLIC.name());
    }

    @Test
    void protectedRouteSetsProtectedRequestType() {
        var captured = captureRequest(exchange("/api/users/v1/me"));
        assertThat(captured.get().getHeaders().getFirst(CommonConstants.Headers.X_REQUEST_TYPE))
                .isEqualTo(RequestType.PROTECTED.name());
    }

    @Test
    void incomingCorrelationIdPropagated() {
        String id = "client-trace-id";
        var captured = captureRequest(exchangeWithCorrelation("/api/users/v1/auth/login", id));
        assertThat(captured.get().getHeaders().getFirst(CommonConstants.Headers.X_CORRELATION_ID)).isEqualTo(id);
    }

    @Test
    void correlationIdGeneratedWhenAbsent() {
        var captured = captureRequest(exchange("/api/users/v1/auth/login"));
        assertThat(captured.get().getHeaders().getFirst(CommonConstants.Headers.X_CORRELATION_ID)).isNotBlank();
    }
}
