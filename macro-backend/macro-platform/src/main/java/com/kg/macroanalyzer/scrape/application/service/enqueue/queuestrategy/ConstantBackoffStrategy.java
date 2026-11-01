package com.kg.macroanalyzer.scrape.application.service.enqueue.queuestrategy;

import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem.ScrapeQueueItemBuilder;

import java.time.Instant;

public class ConstantBackoffStrategy implements QueueTimeStrategy {

    private Integer queueSize;

    public ConstantBackoffStrategy() {
        queueSize = 0;
    }

    @Override
    public ScrapeQueueItemBuilder withTimeSlot(ScrapeQueueItemBuilder scrapeQueueItem) {
        final var MULTIPLIER = 5;
        final var delay = queueSize * MULTIPLIER * 1000 * 60;
        final var scrapeDate = Instant.now().plusMillis(delay);
        queueSize++;

        return scrapeQueueItem.scrapeDate(scrapeDate);
    }

}
