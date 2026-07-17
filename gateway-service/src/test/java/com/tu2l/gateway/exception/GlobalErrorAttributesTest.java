package com.tu2l.gateway.exception;

import com.tu2l.common.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalErrorAttributesTest {

    private GlobalErrorAttributes errorAttributes;

    @BeforeEach
    void setUp() {
        errorAttributes = new GlobalErrorAttributes();
    }

    private ServerRequest requestWith(Throwable t) {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test").build());
        errorAttributes.storeErrorInformation(t, exchange);
        return ServerRequest.create(exchange, Collections.emptyList());
    }

    private Map<String, Object> attrs(Throwable t) {
        return errorAttributes.getErrorAttributes(requestWith(t), ErrorAttributeOptions.defaults());
    }

    @Test
    void connectExceptionMappedTo503() {
        var a = attrs(new ConnectException("refused"));
        assertThat(a.get("status")).isEqualTo(503);
        assertThat(a.get("error")).isEqualTo("Service Unavailable");
    }

    @Test
    void unknownHostMappedTo503() {
        var a = attrs(new UnknownHostException("unknown-host"));
        assertThat(a.get("status")).isEqualTo(503);
    }

    @Test
    void authenticationExceptionMappedTo401() {
        var a = attrs(new AuthenticationException("bad token"));
        assertThat(a.get("status")).isEqualTo(401);
        assertThat(a.get("message")).isEqualTo("bad token");
    }

    @Test
    void genericExceptionMappedTo500() {
        var a = attrs(new RuntimeException("boom"));
        assertThat(a.get("status")).isEqualTo(500);
        assertThat(a.get("message")).isEqualTo("An unexpected error occurred. Please contact support.");
    }

    @Test
    void stackTraceNotIncluded() {
        var a = attrs(new RuntimeException("boom"));
        assertThat(a).doesNotContainKey("trace");
    }
}
