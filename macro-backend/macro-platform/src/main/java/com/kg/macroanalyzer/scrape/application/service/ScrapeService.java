package com.kg.macroanalyzer.scrape.application.service;

import com.kg.macroanalyzer.scrape.application.port.in.ScrapeRun;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeMetricsPort;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Preparer;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.RegisteredScrapeFlow;
import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem;
import com.kg.macroanalyzer.scrape.domain.ScrapeResult;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final ScrapeMetricsPort scrapeMetricsPort;

    @Autowired
    public ScrapeService(
            ScrapeQueuePort scrapeQueuePort,
            List<RegisteredScrapeFlow> scrapeFlows,
            List<SeriesConfig> seriesConfigList,
            Preparer<ScrapeQueueItem, SeriesConfig> seriesConfigPreparer,
            ScrapeMetricsPort scrapeMetricsPort
    ) {
        this.scrapeQueuePort = scrapeQueuePort;
        this.seriesConfigPreparer = seriesConfigPreparer;
        this.scrapeFlows = registerScrapeFlows(scrapeFlows, seriesConfigList);
        this.scrapeMetricsPort = scrapeMetricsPort;
        runIsolatedFromScrapes(() -> scrapeMetricsPort.registerFlows(this.scrapeFlows.keySet()));
    }

    private static Map<String, RegisteredScrapeFlow> registerScrapeFlows(
            List<RegisteredScrapeFlow> scrapeFlows,
            List<SeriesConfig> seriesConfigList
    ) {
        final var registry = new HashMap<String, RegisteredScrapeFlow>();
        for (final var scrapeFlow : scrapeFlows) {
            if (scrapeFlow.flowId() == null) {
                throw new IllegalStateException("Scrape flow %s has no flow id".formatted(scrapeFlow));
            }
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
                .forEach(flowId -> log.warn("Scrape flow {} is registered but no series config uses it", flowId));

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
        log.info("Scraping items from queue: {}", items);

        return items.stream();
    }

    private ScrapeResult scrape(ScrapeQueueItem scrapeQueueItem) {
        final var startedNanos = System.nanoTime();
        final var flowId = findFlowId(scrapeQueueItem);
        final var result = flowId
                .map(id -> run(id, scrapeQueueItem))
                .orElse(ScrapeResult.FAILED);
        final var duration = Duration.ofNanos(System.nanoTime() - startedNanos);
        final var flowOrUnknown = flowId.orElse(ScrapeMetricsPort.UNKNOWN_FLOW);

        if (ScrapeResult.isFailed(result)) {
            log.warn("Scrape item failed: name={} flow={}", scrapeQueueItem.name(), flowOrUnknown);
        }
        runIsolatedFromScrapes(() -> scrapeMetricsPort.recordItem(flowOrUnknown, result, duration));

        return result;
    }

    // An item without a series config stays pending in the queue, as the flows' own preparer would leave it
    private Optional<String> findFlowId(ScrapeQueueItem scrapeQueueItem) {
        try {
            final var flowId = seriesConfigPreparer.prepare(scrapeQueueItem).map(SeriesConfig::scrapeFlow);
            if (flowId.isEmpty()) {
                log.warn("No series config named {}, cannot scrape it", scrapeQueueItem.name());
            }

            return flowId;
        } catch (RuntimeException e) {
            log.error("Scrape of {} failed", scrapeQueueItem.name(), e);

            return Optional.empty();
        }
    }

    // One failing item must not stop the rest of the run. It stays pending and is retried on the next queue drain;
    // items are read oldest first, so an exception escaping here would abort every later run at the same item
    private ScrapeResult run(String flowId, ScrapeQueueItem scrapeQueueItem) {
        try {
            log.info("Routing {} to scrape flow {}", scrapeQueueItem.name(), flowId);

            return scrapeFlows.get(flowId).run(scrapeQueueItem);
        } catch (RuntimeException e) {
            log.error("Scrape of {} failed", scrapeQueueItem.name(), e);

            return ScrapeResult.FAILED;
        }
    }

    private static void runIsolatedFromScrapes(Runnable recording) {
        try {
            recording.run();
        } catch (RuntimeException e) {
            log.warn("Could not record scrape metrics", e);
        }
    }

}
