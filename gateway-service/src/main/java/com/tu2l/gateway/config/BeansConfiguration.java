package com.tu2l.gateway.config;

import com.tu2l.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

@Configuration
public class BeansConfiguration {
    @Value("${jwt.secret-key:dev-default-secret-key-must-be-at-least-32-chars-long-for-HS256}")
    private String secretKey;
    @Value("${jwt.access-token.expiration-minutes:60}")
    private int accessTokenExpirationMinutes;
    @Value("${jwt.refresh-token.expiration-days:30}")
    private int refreshTokenExpirationDays;
    @Value("${jwt.issuer:resume-builder-app}")
    private String issuer;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(secretKey, accessTokenExpirationMinutes, refreshTokenExpirationDays, issuer);
    }

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
}
