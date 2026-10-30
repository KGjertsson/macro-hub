package com.kg.macroanalyzer.election.application.service.scrape;

import com.kg.macroanalyzer.election.application.port.out.ConfigWithLocalElections;
import com.kg.macroanalyzer.election.application.port.out.ElectionRepositoryPort;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Persister;
import com.kg.macroanalyzer.scrape.domain.ScrapeResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record LocalElectionPersister(ElectionRepositoryPort repositoryPort, ScrapeQueuePort scrapeQueuePort) implements Persister<ConfigWithLocalElections> {

    @Override
    public ScrapeResult persist(ConfigWithLocalElections configWithLocalElections) {
        final var config = configWithLocalElections.seriesConfig();
        final var results = configWithLocalElections.results();
        scrapeQueuePort.markAsDone(config);
        if (results.isEmpty()) {
            log.info("Found no new local election results for %s".formatted(config.name()));

            return ScrapeResult.EMPTY;
        }
        final var persistedCount = repositoryPort.writeLocalElectionResults(results);
        log.info("Upserted %s local election areas for %s".formatted(persistedCount, config.name()));

        return ScrapeResult.SUCCESS;
    }

}
