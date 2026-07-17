package com.tu2l.gateway.config;

import com.tu2l.common.util.JwtUtil;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

@Configuration
public class BeansConfiguration {

    @Bean
    public JwtUtil jwtUtil(JwtGatewayProperties jwtProps) {
        return new JwtUtil(jwtProps.secretKey(), jwtProps.accessTokenExpirationMinutes(),
                jwtProps.refreshTokenExpirationDays(), jwtProps.issuer());
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
