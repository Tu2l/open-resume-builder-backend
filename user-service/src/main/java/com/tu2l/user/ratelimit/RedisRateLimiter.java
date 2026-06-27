package com.tu2l.user.ratelimit;

import com.tu2l.user.config.RateLimitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Redis-backed fixed-window rate limiter. Uses an atomic Lua INCR+EXPIRE so the
 * counter and TTL are set in a single round-trip with no race window. Safe under
 * horizontal scaling — all nodes share the same Redis keyspace.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.redis-enabled", havingValue = "true")
public class RedisRateLimiter implements RateLimiter {

    private static final RedisScript<Long> INCREMENT_SCRIPT = RedisScript.of(
            "local c = redis.call('INCR', KEYS[1])\n" +
            "if c == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end\n" +
            "return c",
            Long.class);

    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties properties;

    @Override
    public boolean tryAcquire(String key) {
        if (!properties.enabled()) {
            return true;
        }
        long windowBucket = Instant.now().getEpochSecond() / properties.windowSeconds();
        String redisKey = "rl:" + key + ":" + windowBucket;
        Long count = redisTemplate.execute(INCREMENT_SCRIPT, List.of(redisKey), String.valueOf(properties.windowSeconds()));
        return count != null && count <= properties.capacity();
    }
}
