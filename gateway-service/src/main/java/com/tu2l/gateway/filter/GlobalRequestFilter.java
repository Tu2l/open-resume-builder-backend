package com.tu2l.gateway.filter;

import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.gateway.config.CustomGatewayProperties;
import com.tu2l.gateway.util.WebPathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GlobalRequestFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(GlobalRequestFilter.class);

    private final CustomGatewayProperties gatewayProperties;
    private final WebPathUtil pathUtil;

    public GlobalRequestFilter(CustomGatewayProperties gatewayProperties, WebPathUtil pathUtil) {
        this.pathUtil = pathUtil;
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public int getOrder() {
        return -1; // highest precedence
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Check if path requires authentication
        String path = exchange.getRequest().getURI().getPath();
        if (pathUtil.isPublicRoute(path, gatewayProperties.getPublicRoutes())) {
            // Public route, forward request without authentication
            logger.info("Public route accessed: {}", path);
            return chain.filter(exchange);
        }

        // if protected: extract and validate token then add user info to exchange attributes and forward request
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthenticationException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        // TODO: Validate token and add user information to exchange
        exchange.getAttributes().put("token", token);
        return chain.filter(exchange);
    }
}
