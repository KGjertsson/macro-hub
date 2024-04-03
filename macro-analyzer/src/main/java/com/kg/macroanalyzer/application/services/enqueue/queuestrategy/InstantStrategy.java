package com.kg.macroanalyzer.application.services.enqueue.queuestrategy;


import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem.ScrapeQueueItemBuilder;

import java.time.Instant;

public class InstantStrategy implements QueueTimeStrategy {

    @Override
    public ScrapeQueueItemBuilder withTimeSlot(ScrapeQueueItemBuilder scrapeQueueItem) {
        final var now = Instant.now();

        return scrapeQueueItem.scrapeDate(now);
    }

}
