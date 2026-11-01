package com.kg.macroanalyzer.regionalstatistics.adaptors.web;

import com.kg.macroanalyzer.shared.web.ScrapeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScbRateLimiterTest {

    private final AtomicLong now = new AtomicLong(10_000);
    private final List<Long> sleeps = new ArrayList<>();

    @AfterEach
    void clearInterrupt() {
        Thread.interrupted();
    }

    @Test
    void shouldSpaceCalls_byTheDelay() throws ScrapeException {
        // given
        final var limiter = new ScbRateLimiter(350, now::get, millis -> {
            sleeps.add(millis);
            now.addAndGet(millis);
        });

        // when
        limiter.acquire();
        now.addAndGet(100);
        limiter.acquire();
        now.addAndGet(500);
        limiter.acquire();

        // then
        // The first call goes at once, the second waits the remaining 250 ms, the third is already late enough
        assertEquals(List.of(250L), sleeps);
    }

    @Test
    void shouldKeepTheInterruptFlag_andThrow_whenInterruptedWhileWaiting() throws ScrapeException {
        // given
        final var limiter = new ScbRateLimiter(350, now::get, millis -> {
            throw new InterruptedException();
        });
        limiter.acquire();

        // then
        assertThrows(ScrapeException.class, limiter::acquire);
        assertTrue(Thread.currentThread().isInterrupted());
    }

}
