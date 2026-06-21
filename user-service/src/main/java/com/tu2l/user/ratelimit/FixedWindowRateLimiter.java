package com.tu2l.user.ratelimit;

import com.tu2l.user.config.RateLimitProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight, dependency-free fixed-window rate limiter. Keeps a per-key counter
 * that resets every {@code windowSeconds}. Intended for a single instance; a
 * distributed deployment would back this with Redis instead.
 */
@Component
@RequiredArgsConstructor
public class FixedWindowRateLimiter {

    private final RateLimitProperties properties;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    /**
     * Attempts to consume one request slot for the given key.
     *
     * @return {@code true} if allowed, {@code false} if the limit is exceeded
     */
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

    private static final class Window {
        private final long startWindow;
        private final AtomicInteger count = new AtomicInteger(0);

        private Window(long startWindow) {
            this.startWindow = startWindow;
        }
    }
}
