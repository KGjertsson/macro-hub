package com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister;

import com.kg.macroanalyzer.application.domain.scb.ScbIndicatorResult;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithScbIndicators;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.application.service.ScrapeResult;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Persister;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

// Table stamps are written once, after every indicator: a stamp stored while another indicator on the same table
// still holds old rows would make the scraper skip that indicator. Tables whose fetch failed, and every table of an
// indicator whose write failed, lose their stored stamp, so the indicators using them are fetched again next time.
// The queue item is only marked as done when something was written (or nothing needed writing), so a scrape where
// every write failed is retried on the next queue drain
@Slf4j
public record ScbRegionalPersister(DatabasePort databasePort, ScrapeQueuePort scrapeQueuePort) implements Persister<ConfigWithScbIndicators> {

    @Override
    public ScrapeResult persist(ConfigWithScbIndicators configWithScbIndicators) {
        final var config = configWithScbIndicators.seriesConfig();
        final var results = configWithScbIndicators.results();
        if (results.isEmpty()) {
            log.info("Found no updated SCB tables for %s".formatted(config.name()));
            scrapeQueuePort.markAsDone(config);

            return ScrapeResult.EMPTY;
        }
        final var droppedTables = new HashSet<>(configWithScbIndicators.failedTables());
        final var written = new ArrayList<ScbIndicatorResult>();
        for (final var result : results) {
            try {
                final var persistedCount = databasePort.writeScbIndicatorResult(result);
                written.add(result);
                log.info("Replaced indicator %s with %s area values".formatted(result.indicator().id(), persistedCount));
            } catch (RuntimeException e) {
                log.error("Failed to write SCB indicator %s".formatted(result.indicator().id()), e);
                droppedTables.addAll(result.indicator().tables());
            }
        }
        final var stamps = new HashMap<String, Instant>();
        written.forEach(result -> stamps.putAll(result.tableStamps()));
        stamps.keySet().removeAll(droppedTables);
        databasePort.writeScbTableStamps(stamps, droppedTables);
        if (written.isEmpty()) {
            return ScrapeResult.FAILED;
        }
        scrapeQueuePort.markAsDone(config);

        return ScrapeResult.SUCCESS;
    }

}
