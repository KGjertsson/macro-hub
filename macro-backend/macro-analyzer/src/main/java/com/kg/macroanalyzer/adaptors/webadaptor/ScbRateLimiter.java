package com.kg.macroanalyzer.adaptors.webadaptor;

// SCB's PxWeb API allows 30 calls per 10 seconds. Every call, retries included, waits until delayMs has passed
// since the previous one; the metadata and data adaptors share one instance
public class ScbRateLimiter {

    private final long delayMs;
    private long lastCallMs;

    public ScbRateLimiter(long delayMs) {
        this.delayMs = delayMs;
    }

    public synchronized void acquire() {
        final var waitMs = lastCallMs + delayMs - System.currentTimeMillis();
        if (waitMs > 0) {
            ScbRetry.sleep(waitMs);
        }
        lastCallMs = System.currentTimeMillis();
    }

}
