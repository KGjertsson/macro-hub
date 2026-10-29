package com.kg.macroanalyzer.scrape.application.service.enqueue.queuestrategy;

import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem.ScrapeQueueItemBuilder;

public interface QueueTimeStrategy {

    ScrapeQueueItemBuilder withTimeSlot(ScrapeQueueItemBuilder scrapeQueueItem);

}
