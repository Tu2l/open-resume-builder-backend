package com.tu2l.user.config;

import com.tu2l.common.util.CommonUtil;
import com.tu2l.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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
}