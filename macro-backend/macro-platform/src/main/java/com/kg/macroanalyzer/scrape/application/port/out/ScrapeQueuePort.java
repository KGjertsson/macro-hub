package com.kg.macroanalyzer.scrape.application.port.out;

import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;

import java.time.LocalDateTime;
import java.util.List;

public interface ScrapeQueuePort {

    void persistScrapeQueueItem(ScrapeQueueItem scrapeQueueItem);

    List<ScrapeQueueItem> getScrapeQueue();

    List<ScrapeQueueItem> getItemsToScrape(LocalDateTime timeStamp);

    void markAsDone(SeriesConfig seriesConfig);

}
