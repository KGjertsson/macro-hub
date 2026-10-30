package com.kg.macroanalyzer.scrape.application.service;

import com.kg.macroanalyzer.scrape.application.port.in.ScrapeRun;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.KindedScrapeFlow;
import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem;
import com.kg.macroanalyzer.scrape.domain.ScrapeQueueKind;
import com.kg.macroanalyzer.scrape.domain.ScrapeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

@Slf4j
@Service
public class ScrapeService {

    // Serialises the cron job and manual scrapes; a second caller would re-scrape items still pending in the queue
    private final ReentrantLock scrapeLock = new ReentrantLock();

    private final ScrapeQueuePort scrapeQueuePort;
    private final Map<ScrapeQueueKind, KindedScrapeFlow> scrapeFlows;

    @Autowired
    public ScrapeService(ScrapeQueuePort scrapeQueuePort, List<KindedScrapeFlow> scrapeFlows) {
        this.scrapeQueuePort = scrapeQueuePort;
        this.scrapeFlows = registerScrapeFlows(scrapeFlows);
    }

    // Fails at startup unless every kind has exactly one flow
    private static Map<ScrapeQueueKind, KindedScrapeFlow> registerScrapeFlows(List<KindedScrapeFlow> scrapeFlows) {
        final var registry = new EnumMap<ScrapeQueueKind, KindedScrapeFlow>(ScrapeQueueKind.class);
        for (final var scrapeFlow : scrapeFlows) {
            if (registry.putIfAbsent(scrapeFlow.kind(), scrapeFlow) != null) {
                throw new IllegalStateException(
                        "More than one scrape flow registered for kind %s".formatted(scrapeFlow.kind())
                );
            }
        }

        final var missing = Arrays.stream(ScrapeQueueKind.values())
                .filter(kind -> !registry.containsKey(kind))
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException("No scrape flow registered for kinds %s".formatted(missing));
        }

        return registry;
    }

    public ScrapeRun scrapeFromQueue(LocalDateTime timeStamp) {
        if (!scrapeLock.tryLock()) {
            log.info("Scrape already in progress, skipping");

            return new ScrapeRun.AlreadyRunning();
        }

        try {
            return new ScrapeRun.Completed(fetchItemsToScrape(timeStamp)
                    .map(this::scrape)
                    .toList());
        } finally {
            scrapeLock.unlock();
        }
    }

    private Stream<ScrapeQueueItem> fetchItemsToScrape(LocalDateTime timeStamp) {
        final var items = scrapeQueuePort.getItemsToScrape(timeStamp);
        final var msg = "Scraping items from queue: %s".formatted(items);
        log.info(msg);

        return items.stream();
    }

    private ScrapeResult scrape(ScrapeQueueItem scrapeQueueItem) {
        return scrapeFlows.get(scrapeQueueItem.scrapeQueueKind()).run(scrapeQueueItem);
    }

}
