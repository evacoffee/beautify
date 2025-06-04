package com.evacoffee.beautymod.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RateLimiter<K> {
    private final long maxOperations;
    private final long timeWindow;
    private final TimeUnit timeUnit;
    private final Map<K, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimiter(long maxOperations, long timeWindow, TimeUnit timeUnit) {
        if (maxOperations <= 0 || timeWindow <= 0) {
            throw new IllegalArgumentException("Max operations and time window must be positive");
        }
        this.maxOperations = maxOperations;
        this.timeWindow = timeWindow;
        this.timeUnit = timeUnit;
    }

    public boolean tryAcquire(K key) {
        if (key == null) {
            return false;
        }
        return buckets.computeIfAbsent(key, k -> new TokenBucket()).tryAcquire();
    }

    public void cleanup() {
        long now = System.nanoTime();
        buckets.entrySet().removeIf(entry -> 
            now - entry.getValue().lastAccessTime > timeUnit.toNanos(timeWindow * 2)
        );
    }

    private class TokenBucket {
        private long availableTokens;
        private long lastRefillTime;
        private long lastAccessTime;

        private TokenBucket() {
            this.availableTokens = maxOperations - 1;
            this.lastRefillTime = System.nanoTime();
            this.lastAccessTime = this.lastRefillTime;
        }

        private synchronized boolean tryAcquire() {
            refill();
            lastAccessTime = System.nanoTime();
            
            if (availableTokens > 0) {
                availableTokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.nanoTime();
            long elapsedNanos = now - lastRefillTime;
            long elapsedTime = timeUnit.convert(elapsedNanos, TimeUnit.NANOSECONDS);

            if (elapsedTime >= timeWindow) {
                availableTokens = maxOperations - 1;
                lastRefillTime = now;
            } else {
                double windowNanos = timeUnit.toNanos(timeWindow);
                double refillAmount = (elapsedNanos * maxOperations) / windowNanos;
                
                if (refillAmount > 0) {
                    availableTokens = Math.min(maxOperations, availableTokens + (long) refillAmount);
                    lastRefillTime = now;
                }
            }
        }
    }
}