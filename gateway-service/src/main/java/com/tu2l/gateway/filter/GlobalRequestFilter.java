package com.tu2l.gateway.filter;

import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.gateway.config.CustomGatewayProperties;
import com.tu2l.gateway.util.WebPathUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GlobalRequestFilter implements GlobalFilter, Ordered {

    private final CustomGatewayProperties gatewayProperties;
    private final WebPathUtil pathUtil;

    public GlobalRequestFilter(CustomGatewayProperties gatewayProperties, WebPathUtil pathUtil) {
        this.pathUtil = pathUtil;
        this.gatewayProperties = gatewayProperties;
    }

    @PostConstruct
    public void init() {
        log.info("GlobalRequestFilter initialized with order: {}", getOrder());
        log.info("Public routes configured: {}", gatewayProperties.getPublicRoutes());
        log.info("Service urls routes configured: {}", gatewayProperties.getServiceUrls());
    }

    @Override
    public int getOrder() {
        return -1; // highest precedence
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Check if path requires authentication
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        log.debug("GlobalRequestFilter invoked for {} {}", method, path);
        log.debug("Public routes configured: {}", gatewayProperties.getPublicRoutes());

        if (pathUtil.isPublicRoute(path, gatewayProperties.getPublicRoutes())) {
            // Public route, forward request without authentication
            log.info("Public route accessed: {} {}", method, path);
            return chain.filter(exchange);
        }

        log.info("Protected route accessed: {} {}", method, path);

        // if protected: extract and validate token then add user info to exchange attributes and forward request
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for {} {}", method, path);
            throw new AuthenticationException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        log.debug("Token extracted successfully for {} {}", method, path);
        // TODO: Validate token and add user information to exchange
        exchange.getAttributes().put("token", token);
        return chain.filter(exchange);
    }
}
