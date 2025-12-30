package com.kg.macroanalyzer.application.services.scrape;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;

public interface ScrapeFlowHandle {
    ScrapeResult run(ScrapeQueueItem item);
}
