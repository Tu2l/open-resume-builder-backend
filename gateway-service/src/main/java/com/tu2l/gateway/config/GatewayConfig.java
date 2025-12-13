package com.tu2l.gateway.config;

import com.tu2l.common.constant.ServiceIdentifiers;
import com.tu2l.gateway.filter.AuthGatewayFilter;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

import static com.tu2l.common.constant.ServiceIdentifiers.PDF_SERVICE;
import static com.tu2l.common.constant.ServiceIdentifiers.USER_SERVICE;

@Configuration
public class GatewayConfig {
    @Bean
    public WebProperties.Resources webPropertiesResources() {
        return new WebProperties.Resources();
    }

    @Bean
    public ErrorProperties errorProperties() {
        return new ErrorProperties();
    }

    @Bean
    public AntPathMatcher matcher() {
        return new AntPathMatcher();
    }

    @Bean
    public AuthGatewayFilter authGatewayFilter() {
        return new AuthGatewayFilter();
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder, AuthGatewayFilter authGatewayFilter, CustomGatewayProperties gatewayProperties) {
        return builder.routes()
                // User Service Route
                .route(USER_SERVICE.getServiceName(), r -> r
                        .path("/api" + USER_SERVICE.getBasePath() + "/**")
                        .filters(f -> f
                                .filter(authGatewayFilter)
                                .stripPrefix(1))
                        .uri(gatewayProperties.getServiceUrls().get(USER_SERVICE.getServiceName())))

                // PDF Service Route
                .route(PDF_SERVICE.getServiceName(), r -> r
                        .path("/api" + PDF_SERVICE.getBasePath() + "/**")
                        .filters(f -> f
                                .filter(authGatewayFilter)
                                .stripPrefix(1))
                        .uri(gatewayProperties.getServiceUrls().get(PDF_SERVICE.getServiceName())))
                .build();
    }
}
