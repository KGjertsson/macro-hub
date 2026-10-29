package com.kg.macroanalyzer.adaptors.webadaptor;

import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.WebAdaptorFlow;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import lombok.extern.slf4j.Slf4j;

// Some of SCB's API servers intermittently answer "Non-existent table" (as 404 or 400) for tables that exist, so every
// failure is retried, a little longer each attempt. A 429 waits out the 10 second rate limit window
@Slf4j
final class ScbRetry {

    private static final int ATTEMPTS = 8;
    private static final long BASE_DELAY_MS = 1_000;
    private static final long MAX_DELAY_MS = 5_000;
    private static final long RATE_LIMIT_DELAY_MS = 10_000;

    private ScbRetry() {
    }

    static <T> T fetch(WebAdaptorFlow<String, T> flow, String url, ScbRateLimiter rateLimiter) throws ScrapeException {
        ScrapeException lastException = null;
        for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
            rateLimiter.acquire();
            try {
                return flow.fetch(url);
            } catch (ScrapeException e) {
                lastException = e;
                log.warn("Attempt %d failed for %s: %s".formatted(attempt, url, abbreviate(e.getMessage())));
                if (attempt < ATTEMPTS) {
                    sleep(isRateLimited(e) ? RATE_LIMIT_DELAY_MS : Math.min(MAX_DELAY_MS, BASE_DELAY_MS * attempt));
                }
            }
        }

        throw lastException;
    }

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean isRateLimited(ScrapeException e) {
        return e.getMessage() != null && e.getMessage().startsWith("HTTP 429");
    }

    private static String abbreviate(String message) {
        return message == null || message.length() <= 200 ? message : message.substring(0, 200) + "...";
    }

}
