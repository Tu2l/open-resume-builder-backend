package com.tu2l.gateway.config;

import com.tu2l.gateway.filter.AuthGatewayFilter;
import com.tu2l.gateway.service.AuthGatewayService;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.tu2l.common.constant.ServiceIdentifiers.PDF_SERVICE;
import static com.tu2l.common.constant.ServiceIdentifiers.USER_SERVICE;

@Configuration
public class GatewayConfig {
    public static final String PREFIX_API = "/api";
    public static final String PATTERN_MATCH_ALL = "/**";

    @Bean
    public AuthGatewayFilter authGatewayFilter(AuthGatewayService authGatewayService) {
        return new AuthGatewayFilter(authGatewayService);
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder, AuthGatewayFilter authGatewayFilter, CustomGatewayProperties gatewayProperties) {
        return builder.routes()
                // User Service Route
                .route(USER_SERVICE.getServiceName(), r -> r
                        .path(PREFIX_API + USER_SERVICE.getBasePath() + PATTERN_MATCH_ALL)
                        .filters(f -> f
                                .filter(authGatewayFilter)
                                .stripPrefix(1))
                        .uri(gatewayProperties.getServiceUrls().get(USER_SERVICE.getServiceName())))

                // PDF Service Route
                .route(PDF_SERVICE.getServiceName(), r -> r
                        .path(PREFIX_API + PDF_SERVICE.getBasePath() + PATTERN_MATCH_ALL)
                        .filters(f -> f
                                .filter(authGatewayFilter)
                                .stripPrefix(1))
                        .uri(gatewayProperties.getServiceUrls().get(PDF_SERVICE.getServiceName())))
                .build();
    }
}
