package com.tu2l.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Rate-limiting settings for sensitive authentication endpoints.
 *
 * @param enabled       whether rate limiting is active
 * @param capacity      max requests allowed per client within a window
 * @param windowSeconds length of the fixed window, in seconds
 */
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        boolean enabled,
        int capacity,
        int windowSeconds
) {
}
