package com.kg.macroanalyzer.scrape.application.service.enqueue.queuestrategy;

import java.time.Instant;

public interface QueueTimeStrategy {

    Instant scrapeDate(Instant now, int queuePosition);

}
