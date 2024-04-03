package com.kg.macroanalyzer.application.services.enqueue.queuestrategy;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem.ScrapeQueueItemBuilder;

public interface QueueTimeStrategy {

    ScrapeQueueItemBuilder withTimeSlot(ScrapeQueueItemBuilder scrapeQueueItem);

}
