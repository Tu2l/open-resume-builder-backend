package com.tu2l.user.ratelimit;

public interface RateLimiter {
    boolean tryAcquire(String key);
}
