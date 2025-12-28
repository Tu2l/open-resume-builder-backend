package com.tu2l.gateway.filter;

import com.tu2l.common.constant.CommonConstants;
import com.tu2l.common.constant.RequestType;
import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.gateway.service.AuthGatewayService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
public class AuthGatewayFilter implements GatewayFilter, Ordered {
    private final AuthGatewayService authService;

    public AuthGatewayFilter(AuthGatewayService authService) {
        this.authService = authService;
    }

    @NonNull
    @Override
    public Mono<@NonNull Void> filter(ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();
        HttpHeaders headers = request.getHeaders();
        String requestTypeString = headers.getFirst(CommonConstants.Headers.X_REQUEST_TYPE);

        log.debug("Processing authentication for {} {}", method, path);

        /*
         * GlobalRequestFilter verifies the path and sets X-Request-Type header.
         * Missing or invalid header indicates a filter chain issue.
         */
        RequestType requestType = RequestType.parse(requestTypeString);
        if (requestType == RequestType.UNDEFINED) {
            log.error("Missing or invalid X-Request-Type header for {} {} - GlobalRequestFilter may have failed",
                    method, path);
            throw new AuthenticationException("Invalid request type configuration");
        }

        if (requestType == RequestType.PROTECTED) {
            String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
            if (StringUtil.isNullOrEmpty(authHeader) || !authHeader.startsWith(CommonConstants.Token.TOKEN_TYPE_BEARER)) {
                log.warn("Missing or invalid Authorization header for {} {}", method, path);
                throw new AuthenticationException("Missing or invalid Authorization header");
            }
            String token = authHeader.substring(CommonConstants.Token.BEARER_PREFIX_LENGTH);

            validateToken(token, method, path);

            ServerHttpRequest mutatedRequest = enrichRequestWithUserInfo(request, token, method, path);
            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

            log.info("Protected request authenticated: {} {}", method, path);
            return chain.filter(mutatedExchange);
        }

        log.debug("Public request allowed: {} {}", method, path);
        return chain.filter(exchange);
    }

    private ServerHttpRequest enrichRequestWithUserInfo(ServerHttpRequest request, String token,
                                                        HttpMethod method, String path) {
        try {
            ServerHttpRequest mutatedRequest = authService.mutateRequestWithUserInfo(request, token);
            log.debug("Request enriched with user info for {} {}", method, path);
            return mutatedRequest;
        } catch (Exception e) {
            log.error("Failed to enrich request with user info for {} {}", method, path, e);
            throw new AuthenticationException("Failed to extract user information from token", e);
        }
    }

    private void validateToken(String token, HttpMethod method, String path) {
        try {
            if (!authService.validateToken(token)) {
                throw new AuthenticationException("Invalid or expired token");
            }
            log.debug("Token validated for {} {}", method, path);
        } catch (AuthenticationException e) {
            throw e;
        } catch (ExpiredJwtException expiredJwtException) {
            throw new AuthenticationException("Token expired", expiredJwtException);
        } catch (JwtException jwtException) {
            throw new AuthenticationException("Token validation error", jwtException);
        } catch (Exception e) {
            log.error("Token validation error for {} {}", method, path, e);
            throw new AuthenticationException("Token validation error", e);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1; // run after GlobalRequestFilter
    }
}
