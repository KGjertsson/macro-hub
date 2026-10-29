package com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister;

import com.kg.macroanalyzer.application.ports.driven.ConfigWithLocalElections;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.application.service.ScrapeResult;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Persister;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record LocalElectionPersister(DatabasePort databasePort, ScrapeQueuePort scrapeQueuePort) implements Persister<ConfigWithLocalElections> {

    @Override
    public ScrapeResult persist(ConfigWithLocalElections configWithLocalElections) {
        final var config = configWithLocalElections.seriesConfig();
        final var results = configWithLocalElections.results();
        scrapeQueuePort.markAsDone(config);
        if (results.isEmpty()) {
            log.info("Found no new local election results for %s".formatted(config.name()));

            return ScrapeResult.EMPTY;
        }
        final var persistedCount = databasePort.writeLocalElectionResults(results);
        log.info("Upserted %s local election areas for %s".formatted(persistedCount, config.name()));

        return ScrapeResult.SUCCESS;
    }

}
