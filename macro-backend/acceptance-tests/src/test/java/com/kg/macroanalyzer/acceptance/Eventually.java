package com.kg.macroanalyzer.acceptance;

import java.time.Duration;
import java.time.Instant;

public final class Eventually {

    private static final Duration BETWEEN_TRIES = Duration.ofMillis(250);

    private Eventually() {
    }

    public static void assertWithin(Duration timeout, String expectation, Runnable assertion) {
        final var deadline = Instant.now().plus(timeout);
        while (true) {
            try {
                assertion.run();
                return;
            } catch (AssertionError lastFailure) {
                if (Instant.now().isAfter(deadline)) {
                    throw new AssertionError(
                            "%s, not within %s. Last try: %s".formatted(expectation, timeout, lastFailure.getMessage()),
                            lastFailure);
                }
            }
            sleep();
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(BETWEEN_TRIES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

}
