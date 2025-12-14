package com.tu2l.gateway.filter;

import com.tu2l.common.constant.CommonConstants;
import com.tu2l.common.constant.RequestType;
import com.tu2l.gateway.config.CustomGatewayProperties;
import com.tu2l.gateway.util.WebPathUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
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
        log.info(
                "GlobalRequestFilter initialized with order: {}, public routes: {}, service urls: {}",
                getOrder(),
                gatewayProperties.getPublicRoutes(),
                gatewayProperties.getServiceUrls()
        );
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // highest precedence
    }

    @NonNull
    @Override
    public Mono<@NonNull Void> filter(ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        // Check if path requires authentication
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        RequestType requestType = pathUtil.isPublicRoute(path, gatewayProperties.getPublicRoutes())
                ? RequestType.PUBLIC
                : RequestType.PROTECTED;

        log.debug("Processing {} {} as {}", method, path, requestType);

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(CommonConstants.Headers.X_REQUEST_TYPE, requestType.name())
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
        return chain.filter(mutatedExchange);
    }
}
