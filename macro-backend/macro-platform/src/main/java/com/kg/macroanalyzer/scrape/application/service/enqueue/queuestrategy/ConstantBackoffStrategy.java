package com.kg.macroanalyzer.scrape.application.service.enqueue.queuestrategy;

import java.time.Duration;
import java.time.Instant;

public class ConstantBackoffStrategy implements QueueTimeStrategy {

    private static final Duration BACKOFF = Duration.ofMinutes(5);

    @Override
    public Instant scrapeDate(Instant now, int queuePosition) {
        return now.plus(BACKOFF.multipliedBy(queuePosition));
    }

}
