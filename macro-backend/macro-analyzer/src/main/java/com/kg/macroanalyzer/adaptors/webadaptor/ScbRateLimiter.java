package com.kg.macroanalyzer.adaptors.webadaptor;

import com.kg.macroanalyzer.shared.web.ScrapeException;

import java.util.function.LongSupplier;

// SCB's PxWeb API allows 30 calls per 10 seconds. Every call, retries included, waits until delayMs has passed
// since the previous one; the metadata and data adaptors share one instance
public class ScbRateLimiter {

    private final long delayMs;
    private final LongSupplier clock;
    private final Sleeper sleeper;
    private long lastCallMs = Long.MIN_VALUE / 2;

    public ScbRateLimiter(long delayMs) {
        this(delayMs, System::currentTimeMillis, Sleeper.THREAD);
    }

    ScbRateLimiter(long delayMs, LongSupplier clock, Sleeper sleeper) {
        this.delayMs = delayMs;
        this.clock = clock;
        this.sleeper = sleeper;
    }

    public synchronized void acquire() throws ScrapeException {
        final var waitMs = lastCallMs + delayMs - clock.getAsLong();
        if (waitMs > 0) {
            Sleeper.sleepOrStop(sleeper, waitMs);
        }
        lastCallMs = clock.getAsLong();
    }

}
