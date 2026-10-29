package com.kg.macroanalyzer.adaptors.webadaptor;

import com.kg.macroanalyzer.shared.web.ScrapeException;
import com.kg.macroanalyzer.shared.web.WebAdaptorFlow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScbRetryTest {

    private static final String URL = "https://statistikdatabasen.scb.se/api/v2/tables/TAB6574/metadata?lang=sv";

    private final List<Long> sleeps = new ArrayList<>();
    private final AtomicInteger calls = new AtomicInteger();
    private final ScbRateLimiter noLimit = new ScbRateLimiter(0, () -> 0L, millis -> {
    });

    @AfterEach
    void clearInterrupt() {
        Thread.interrupted();
    }

    @Test
    void shouldReturn_afterTransientFailures_withGrowingBackoff() throws ScrapeException {
        // given
        final var retry = new ScbRetry(noLimit, sleeps::add);
        final var flow = flow("HTTP 404 from upstream: Non-existent table", 2);

        // when
        final var response = retry.fetch(flow, URL);

        // then
        assertEquals("ok", response);
        assertEquals(3, calls.get());
        assertEquals(List.of(1_000L, 2_000L), sleeps);
    }

    @Test
    void shouldWaitOutTheRateLimitWindow_after429() throws ScrapeException {
        // given
        final var retry = new ScbRetry(noLimit, sleeps::add);

        // when
        retry.fetch(flow("HTTP 429 from upstream: ", 1), URL);

        // then
        assertEquals(List.of(ScbRetry.RATE_LIMIT_DELAY_MS), sleeps);
    }

    @Test
    void shouldGiveUp_afterEightAttempts_withCappedBackoff() {
        // given
        final var retry = new ScbRetry(noLimit, sleeps::add);

        // when
        final var exception = assertThrows(ScrapeException.class, () -> retry.fetch(flow("HTTP 500 from upstream: ", 100), URL));

        // then
        assertEquals("HTTP 500 from upstream: ", exception.getMessage());
        assertEquals(ScbRetry.ATTEMPTS, calls.get());
        assertEquals(List.of(1_000L, 2_000L, 3_000L, 4_000L, 5_000L, 5_000L, 5_000L), sleeps);
    }

    @Test
    void shouldStopRetrying_whenInterruptedDuringBackoff() {
        // given
        final var retry = new ScbRetry(noLimit, millis -> {
            throw new InterruptedException();
        });

        // when
        assertThrows(ScrapeException.class, () -> retry.fetch(flow("HTTP 404 from upstream: ", 100), URL));

        // then
        assertEquals(1, calls.get());
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void shouldNotCall_whenTheThreadIsAlreadyInterrupted() {
        // given
        final var retry = new ScbRetry(noLimit, sleeps::add);
        Thread.currentThread().interrupt();

        // when
        assertThrows(ScrapeException.class, () -> retry.fetch(flow("unused", 0), URL));

        // then
        assertEquals(0, calls.get());
    }

    // Fails with the given message the given number of times, then answers "ok"
    private WebAdaptorFlow<String, String> flow(String failure, int failures) {
        return new WebAdaptorFlow<>(
                url -> null,
                connection -> {
                    if (calls.incrementAndGet() <= failures) {
                        throw new ScrapeException(failure);
                    }
                    return "ok";
                },
                response -> response
        );
    }

}
