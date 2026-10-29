package com.kg.macroanalyzer.adaptors.webadaptor;

import com.kg.macroanalyzer.shared.web.ScrapeException;
import com.kg.macroanalyzer.shared.web.WebAdaptorFlow;
import lombok.extern.slf4j.Slf4j;

// Some of SCB's API servers intermittently answer "Non-existent table" (as 404 or 400) for tables that exist, so every
// failure is retried, a little longer each attempt. A 429 waits out the 10 second rate limit window.
// An interrupted thread stops retrying
@Slf4j
final class ScbRetry {

    static final int ATTEMPTS = 8;
    static final long BASE_DELAY_MS = 1_000;
    static final long MAX_DELAY_MS = 5_000;
    static final long RATE_LIMIT_DELAY_MS = 10_000;

    private final ScbRateLimiter rateLimiter;
    private final Sleeper sleeper;

    ScbRetry(ScbRateLimiter rateLimiter) {
        this(rateLimiter, Sleeper.THREAD);
    }

    ScbRetry(ScbRateLimiter rateLimiter, Sleeper sleeper) {
        this.rateLimiter = rateLimiter;
        this.sleeper = sleeper;
    }

    <T> T fetch(WebAdaptorFlow<String, T> flow, String url) throws ScrapeException {
        ScrapeException lastException = null;
        for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
            if (Thread.currentThread().isInterrupted()) {
                throw new ScrapeException("Interrupted before calling SCB for " + url);
            }
            rateLimiter.acquire();
            try {
                return flow.fetch(url);
            } catch (ScrapeException e) {
                lastException = e;
                log.warn("Attempt %d failed for %s: %s".formatted(attempt, url, abbreviate(e.getMessage())));
                if (attempt < ATTEMPTS) {
                    Sleeper.sleepOrStop(sleeper, backoffMs(e, attempt));
                }
            }
        }

        throw lastException;
    }

    private static long backoffMs(ScrapeException e, int attempt) {
        return isRateLimited(e) ? RATE_LIMIT_DELAY_MS : Math.min(MAX_DELAY_MS, BASE_DELAY_MS * attempt);
    }

    private static boolean isRateLimited(ScrapeException e) {
        return e.getMessage() != null && e.getMessage().startsWith("HTTP 429");
    }

    private static String abbreviate(String message) {
        return message == null || message.length() <= 200 ? message : message.substring(0, 200) + "...";
    }

}
