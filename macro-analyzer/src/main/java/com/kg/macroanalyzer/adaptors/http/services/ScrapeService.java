package com.kg.macroanalyzer.adaptors.http.services;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeEngine.ScrapeEngine;
import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeEngine.ScrapeEngineFactory;
import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.adaptors.database.postgres.repositories.ScrapeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Service
public class ScrapeService {

    private final ScrapeRepository scrapeRepository;
    private final ScrapeEngineFactory scrapeEngineFactory;

    @Autowired
    public ScrapeService(ScrapeRepository scrapeRepository, ScrapeEngineFactory scrapeEngineFactory) {
        this.scrapeRepository = scrapeRepository;
        this.scrapeEngineFactory = scrapeEngineFactory;
    }

    public void scrapeFromQueue(LocalDateTime timeStamp) {
        fetchItemsToScrape(timeStamp)
                .map(scrapeEngineFactory::createScrapeEngine)
                .filter(Objects::nonNull)
                .forEach(ScrapeEngine::scrape);
    }

    private Stream<ScrapeQueueItem> fetchItemsToScrape(LocalDateTime timeStamp) {
        final var items = scrapeRepository.getItemsToScrape(timeStamp);
        log.info("Scraping items from queue: %s".formatted(items));

        return items.stream();
    }

}
