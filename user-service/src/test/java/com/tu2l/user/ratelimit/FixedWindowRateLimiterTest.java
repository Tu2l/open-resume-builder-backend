package com.tu2l.user.ratelimit;

import com.tu2l.user.config.RateLimitProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FixedWindowRateLimiterTest {

    @Test
    void tryAcquire_allowsUpToCapacityThenBlocks() {
        var limiter = new FixedWindowRateLimiter(new RateLimitProperties(true, 3, 60));

        assertThat(limiter.tryAcquire("ip:/auth")).isTrue();
        assertThat(limiter.tryAcquire("ip:/auth")).isTrue();
        assertThat(limiter.tryAcquire("ip:/auth")).isTrue();
        assertThat(limiter.tryAcquire("ip:/auth")).isFalse();
    }

    @Test
    void tryAcquire_alwaysAllowsWhenDisabled() {
        var limiter = new FixedWindowRateLimiter(new RateLimitProperties(false, 1, 60));

        assertThat(limiter.tryAcquire("ip:/auth")).isTrue();
        assertThat(limiter.tryAcquire("ip:/auth")).isTrue();
    }

    @Test
    void evictStaleWindows_removesEntriesFromElapsedWindows() {
        // A 1-second window guarantees the entry's window has elapsed by the time we sweep.
        var limiter = new FixedWindowRateLimiter(new RateLimitProperties(true, 5, 1));
        limiter.tryAcquire("ip:/auth");

        await(1100);
        limiter.evictStaleWindows();

        // Counter is gone, so a fresh client gets the full capacity again.
        for (int i = 0; i < 5; i++) {
            assertThat(limiter.tryAcquire("ip:/auth")).isTrue();
        }
    }

    private static void await(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
