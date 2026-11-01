package com.kg.macroanalyzer.scrape.application.service.enqueue.queuestrategy;


import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem.ScrapeQueueItemBuilder;

import java.time.Instant;

public class InstantStrategy implements QueueTimeStrategy {

    @Override
    public ScrapeQueueItemBuilder withTimeSlot(ScrapeQueueItemBuilder scrapeQueueItem) {
        final var now = Instant.now();

        return scrapeQueueItem.scrapeDate(now);
    }

}
