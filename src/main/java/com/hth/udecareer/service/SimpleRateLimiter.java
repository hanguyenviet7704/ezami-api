package com.hth.udecareer.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SimpleRateLimiter {
    private static class Bucket {
        long tokens;
        Instant lastRefill;
        Bucket(long tokens, Instant lastRefill) { this.tokens = tokens; this.lastRefill = lastRefill; }
    }

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final long maxTokens = 10;
    private final long refillSeconds = 60; // refill every minute

    public synchronized boolean tryConsume(String key) {
        Instant now = Instant.now();
        Bucket b = buckets.computeIfAbsent(key, k -> new Bucket(maxTokens, now));
        // refill tokens
        long secondsSince = now.getEpochSecond() - b.lastRefill.getEpochSecond();
        if (secondsSince > 0) {
            long refill = (secondsSince / refillSeconds) * maxTokens; // refill per minute
            if (refill > 0) {
                b.tokens = Math.min(maxTokens, b.tokens + refill);
                b.lastRefill = now;
            }
        }
        if (b.tokens > 0) {
            b.tokens--;
            return true;
        }
        return false;
    }
}
