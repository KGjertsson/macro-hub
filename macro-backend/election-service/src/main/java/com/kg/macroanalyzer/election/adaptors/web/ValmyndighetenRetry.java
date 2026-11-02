package com.kg.macroanalyzer.election.adaptors.web;

import com.kg.macroanalyzer.shared.web.ScrapeException;
import com.kg.macroanalyzer.shared.web.WebAdaptorFlow;
import lombok.extern.slf4j.Slf4j;

// resultat.val.se answers 429 when called too often, so failed calls back off a little longer each attempt
@Slf4j
final class ValmyndighetenRetry {

    private static final int ATTEMPTS = 3;
    private static final long BASE_DELAY_MS = 2_000;

    private ValmyndighetenRetry() {
    }

    static <T> T fetch(WebAdaptorFlow<String, T> flow, String url) throws ScrapeException {
        ScrapeException lastException = null;
        for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
            try {
                return flow.fetch(url);
            } catch (ScrapeException e) {
                lastException = e;
                log.warn("Attempt %d failed for %s: %s".formatted(attempt, url, e.getMessage()));
                if (attempt < ATTEMPTS) {
                    sleep(BASE_DELAY_MS * attempt);
                }
            }
        }

        throw lastException;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
