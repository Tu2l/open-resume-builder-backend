package com.tu2l.user.ratelimit;

import com.tu2l.user.config.RateLimitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight, dependency-free fixed-window rate limiter. Keeps a per-key counter
 * that resets every {@code windowSeconds}. Intended for a single instance; a
 * distributed deployment would back this with Redis instead.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.redis-enabled", havingValue = "false", matchIfMissing = true)
public class FixedWindowRateLimiter implements RateLimiter {

    private final RateLimitProperties properties;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    /**
     * Attempts to consume one request slot for the given key.
     *
     * @return {@code true} if allowed, {@code false} if the limit is exceeded
     */
    @Override
    public boolean tryAcquire(String key) {
        if (!properties.enabled()) {
            return true;
        }
        long currentWindow = Instant.now().getEpochSecond() / properties.windowSeconds();
        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || existing.startWindow != currentWindow) {
                return new Window(currentWindow);
            }
            return existing;
        });
        return window.count.incrementAndGet() <= properties.capacity();
    }

    /**
     * Removes windows whose fixed window has fully elapsed. Without this, keys for
     * one-off client IPs accumulate forever (an attacker-controlled memory leak).
     * Runs periodically; the interval is independent of the rate-limit window.
     */
    @Scheduled(fixedDelayString = "${app.rate-limit.cleanup-interval-ms:300000}")
    public void evictStaleWindows() {
        long currentWindow = Instant.now().getEpochSecond() / properties.windowSeconds();
        int before = windows.size();
        windows.entrySet().removeIf(entry -> entry.getValue().startWindow < currentWindow);
        int removed = before - windows.size();
        if (removed > 0) {
            log.debug("Evicted {} stale rate-limit window(s)", removed);
        }
    }

    private static final class Window {
        private final long startWindow;
        private final AtomicInteger count = new AtomicInteger(0);

        private Window(long startWindow) {
            this.startWindow = startWindow;
        }
    }
}
