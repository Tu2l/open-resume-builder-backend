package com.tu2l.gateway.filter;

import org.jspecify.annotations.NonNull;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class AuthGatewayFilter implements GatewayFilter, Ordered {
    @NonNull
    @Override
    public Mono<@NonNull Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        // TODO
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
