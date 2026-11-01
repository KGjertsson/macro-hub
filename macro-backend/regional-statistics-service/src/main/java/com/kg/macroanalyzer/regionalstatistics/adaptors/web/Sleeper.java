package com.kg.macroanalyzer.regionalstatistics.adaptors.web;

import com.kg.macroanalyzer.shared.web.ScrapeException;

// Lets tests run the SCB retry and rate limiter without waiting
@FunctionalInterface
interface Sleeper {

    Sleeper THREAD = Thread::sleep;

    void sleep(long millis) throws InterruptedException;

    // An interrupt (e.g. shutdown) keeps the thread's interrupt flag and ends the scrape instead of being swallowed
    static void sleepOrStop(Sleeper sleeper, long millis) throws ScrapeException {
        try {
            sleeper.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ScrapeException("Interrupted while waiting to call SCB");
        }
    }

}
