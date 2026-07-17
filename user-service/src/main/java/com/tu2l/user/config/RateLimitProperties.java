package com.tu2l.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * Rate-limiting settings for sensitive authentication endpoints. Fully config-driven so
 * the filter can be reused across services without code changes.
 *
 * @param enabled           whether rate limiting is active
 * @param capacity          max requests allowed per client within a window
 * @param windowSeconds     length of the fixed window, in seconds
 * @param limitedPaths      request-URI suffixes to rate-limit (empty = none)
 * @param cleanupIntervalMs how often to evict stale windows, in milliseconds
 */
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("10") int capacity,
        @DefaultValue("60") int windowSeconds,
        @DefaultValue List<String> limitedPaths,
        @DefaultValue("300000") long cleanupIntervalMs,
        @DefaultValue List<String> trustedProxies
) {
}
