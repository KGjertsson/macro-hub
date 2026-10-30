package com.kg.macroanalyzer.scrape.application.service;

import com.kg.macroanalyzer.scrape.application.port.in.ScrapeRun;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Preparer;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.RegisteredScrapeFlow;
import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem;
import com.kg.macroanalyzer.scrape.domain.ScrapeResult;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ScrapeService {

    // Serialises the cron job and manual scrapes; a second caller would re-scrape items still pending in the queue
    private final ReentrantLock scrapeLock = new ReentrantLock();

    private final ScrapeQueuePort scrapeQueuePort;
    private final Preparer<ScrapeQueueItem, SeriesConfig> seriesConfigPreparer;
    private final Map<String, RegisteredScrapeFlow> scrapeFlows;

    @Autowired
    public ScrapeService(
            ScrapeQueuePort scrapeQueuePort,
            List<RegisteredScrapeFlow> scrapeFlows,
            List<SeriesConfig> seriesConfigList,
            Preparer<ScrapeQueueItem, SeriesConfig> seriesConfigPreparer
    ) {
        this.scrapeQueuePort = scrapeQueuePort;
        this.seriesConfigPreparer = seriesConfigPreparer;
        this.scrapeFlows = registerScrapeFlows(scrapeFlows, seriesConfigList);
    }

    // Fails at startup if two flows share an id or a series config names a flow that is not registered
    private static Map<String, RegisteredScrapeFlow> registerScrapeFlows(
            List<RegisteredScrapeFlow> scrapeFlows,
            List<SeriesConfig> seriesConfigList
    ) {
        final var registry = new HashMap<String, RegisteredScrapeFlow>();
        for (final var scrapeFlow : scrapeFlows) {
            if (registry.putIfAbsent(scrapeFlow.flowId(), scrapeFlow) != null) {
                throw new IllegalStateException(
                        "More than one scrape flow registered for flow id %s".formatted(scrapeFlow.flowId())
                );
            }
        }

        final var unregistered = seriesConfigList.stream()
                .filter(c -> !registry.containsKey(c.scrapeFlow()))
                .map(c -> "%s (scrapeFlow=%s)".formatted(c.name(), c.scrapeFlow()))
                .toList();
        if (!unregistered.isEmpty()) {
            throw new IllegalStateException(
                    "No scrape flow registered for series configs %s; registered flow ids are %s"
                            .formatted(unregistered, registry.keySet())
            );
        }

        final var usedFlowIds = seriesConfigList.stream()
                .map(SeriesConfig::scrapeFlow)
                .collect(Collectors.toSet());
        registry.keySet().stream()
                .filter(flowId -> !usedFlowIds.contains(flowId))
                .sorted()
                .forEach(flowId -> log.warn("Scrape flow %s is registered but no series config uses it".formatted(flowId)));

        return Map.copyOf(registry);
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

    // An item without a series config stays pending in the queue, as the flows' own preparer would leave it
    private ScrapeResult scrape(ScrapeQueueItem scrapeQueueItem) {
        return seriesConfigPreparer.prepare(scrapeQueueItem)
                .map(SeriesConfig::scrapeFlow)
                .map(flowId -> {
                    log.info("Routing %s to scrape flow %s".formatted(scrapeQueueItem.name(), flowId));
                    return scrapeFlows.get(flowId).run(scrapeQueueItem);
                })
                .orElseGet(() -> {
                    log.warn("No series config named %s, cannot scrape it".formatted(scrapeQueueItem.name()));
                    return ScrapeResult.FAILED;
                });
    }

}
