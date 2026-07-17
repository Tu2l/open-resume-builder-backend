package com.tu2l.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth.jwt")
public record JwtConfig(String secretKey, long accessTokenExpirationMinutes, int refreshTokenExpirationDays,
                        String issuer) {
}
