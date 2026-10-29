package com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister;

import com.kg.macroanalyzer.application.ports.driven.ConfigWithScbIndicators;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.services.scrape.ScrapeResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record ScbRegionalPersister(DatabasePort databasePort) implements Persister<ConfigWithScbIndicators> {

    @Override
    public ScrapeResult persist(ConfigWithScbIndicators configWithScbIndicators) {
        final var config = configWithScbIndicators.seriesConfig();
        final var results = configWithScbIndicators.results();
        databasePort.markAsDone(config);
        if (results.isEmpty()) {
            log.info("Found no updated SCB tables for %s".formatted(config.name()));

            return ScrapeResult.EMPTY;
        }
        results.forEach(result -> {
            final var persistedCount = databasePort.writeScbIndicatorResult(result);
            log.info("Replaced indicator %s with %s area values".formatted(result.indicator().id(), persistedCount));
        });

        return ScrapeResult.SUCCESS;
    }

}
