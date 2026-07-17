package com.tu2l.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtGatewayProperties(String secretKey, int accessTokenExpirationMinutes,
                                   int refreshTokenExpirationDays, String issuer) {
}
