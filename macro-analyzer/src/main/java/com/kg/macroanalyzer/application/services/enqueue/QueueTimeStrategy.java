package com.kg.macroanalyzer.application.services.enqueue;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem.ScrapeQueueItemBuilder;

import java.time.Instant;

public class QueueTimeStrategy {

    private Integer queueSize;

    public QueueTimeStrategy() {
        queueSize = 0;
    }

    public ScrapeQueueItemBuilder withTimeSlot(ScrapeQueueItemBuilder scrapeQueueItem) {
        final var MULTIPLIER = 5;
        final var delay = queueSize * MULTIPLIER * 1000 * 60;
        final var scrapeDate = Instant.now().plusMillis(delay);
        queueSize++;

        return scrapeQueueItem.scrapeDate(scrapeDate);
    }

}
