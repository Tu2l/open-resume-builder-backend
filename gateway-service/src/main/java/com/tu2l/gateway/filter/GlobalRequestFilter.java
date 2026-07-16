package com.tu2l.gateway.filter;

import com.tu2l.common.constant.CommonConstants;
import com.tu2l.common.constant.RequestType;
import com.tu2l.gateway.config.CustomGatewayProperties;
import com.tu2l.gateway.util.WebPathUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalRequestFilter implements GlobalFilter {

    static final String MDC_KEY = "requestId";

    private final CustomGatewayProperties gatewayProperties;
    private final WebPathUtil pathUtil;

    public GlobalRequestFilter(CustomGatewayProperties gatewayProperties, WebPathUtil pathUtil) {
        this.pathUtil = pathUtil;
        this.gatewayProperties = gatewayProperties;
    }

    @PostConstruct
    public void init() {
        log.info(
                "GlobalRequestFilter initialized with public routes: {}, service URLs: {}",
                gatewayProperties.getPublicRoutes(),
                gatewayProperties.getServiceUrls()
        );
    }

    @NonNull
    @Override
    public Mono<@NonNull Void> filter(ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        // Check if path requires authentication
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        RequestType requestType = pathUtil.isPublicRoute(path, gatewayProperties.getPublicRoutes())
                ? RequestType.PUBLIC
                : RequestType.PROTECTED;

        String correlationId = UUID.randomUUID().toString();

        // 1. Mutate the request headers to pass downstream to microservices
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(CommonConstants.Headers.X_REQUEST_TYPE, requestType.name())
                .header(CommonConstants.Headers.X_CORRELATION_ID, correlationId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        // 2. Put MDC value for the *current* synchronous logging footprint
        MDC.put(MDC_KEY, correlationId);
        log.debug("Processing {} {} as {}", method, path, requestType);

        // 3. Wrap the downstream execution chain in a reactive lifecycle block
        return chain.filter(mutatedExchange)
                .doFirst(() -> {
                    // Ensures the ID is present if immediate downstream execution stays on this thread
                    MDC.put(MDC_KEY, correlationId);
                })
                .doFinally(signalType -> {
                    // CRITICAL: Guaranteed cleanup when the request completes, errors, or cancels
                    MDC.remove(MDC_KEY);
                })
                // Optional: Integrates the ID directly into the reactive context
                // for downstream WebFlux loggers that support context propagation
                .contextWrite(context -> context.put(MDC_KEY, correlationId));
    }
}