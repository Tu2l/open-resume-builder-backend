package com.tu2l.user.config;

import com.tu2l.common.util.CommonUtil;
import com.tu2l.common.util.JwtUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class BeansConfiguration {
    private final CorsProperties corsProperties;

    @Value("${jwt.secret-key:dev-default-secret-key-must-be-at-least-32-chars-long-for-HS256}")
    private String secretKey;
    @Value("${jwt.access-token.expiration-minutes:60}")
    private int accessTokenExpirationMinutes;
    @Value("${jwt.refresh-token.expiration-days:30}")
    private int refreshTokenExpirationDays;
    @Value("${jwt.issuer:resume-builder-app}")
    private String issuer;

    public BeansConfiguration(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommonUtil commonUtil() {
        return new CommonUtil();
    }

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(secretKey, accessTokenExpirationMinutes, refreshTokenExpirationDays, issuer);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                WebMvcConfigurer.super.addCorsMappings(registry);
                registry.addMapping("/**")
                        .allowedOrigins(corsProperties.getAllowedOrigins().toArray(new String[0]))
                        .allowedMethods(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name(), HttpMethod.PATCH.name())
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .exposedHeaders("Authorization", "Content-Type")
                        .maxAge(3600);
            }
        };
    }
}