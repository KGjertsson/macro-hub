package com.kg.macroanalyzer.scrape.adaptors.postgres;

import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class ScrapeQueuePostgresAdaptor implements ScrapeQueuePort {

    private final ScrapeRepository scrapeRepository;

    public ScrapeQueuePostgresAdaptor(ScrapeRepository scrapeRepository) {
        this.scrapeRepository = scrapeRepository;
    }

    @Override
    public void persistScrapeQueueItem(ScrapeQueueItem scrapeQueueItem) {
        log.info("Persisting scrapeQueueItem=%s".formatted(scrapeQueueItem));

        scrapeRepository.addScrapeQueueItem(scrapeQueueItem);
    }

    @Override
    public List<ScrapeQueueItem> getScrapeQueue() {
        return scrapeRepository.getCurrentQueue();
    }

    @Override
    public List<ScrapeQueueItem> getItemsToScrape(LocalDateTime timeStamp) {
        return scrapeRepository.getItemsToScrape(timeStamp);
    }

    @Override
    public void markAsDone(SeriesConfig seriesConfig) {
        scrapeRepository.markAsDone(seriesConfig.name());
    }

}
