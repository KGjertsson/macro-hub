package com.kg.macroanalyzer.election.application.service.scrape;

import com.kg.macroanalyzer.election.application.port.out.ConfigWithLocalElections;
import com.kg.macroanalyzer.election.application.port.out.ElectionRepositoryPort;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Persister;
import com.kg.macroanalyzer.scrape.domain.ScrapeResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record LocalElectionPersister(
        ElectionRepositoryPort repositoryPort,
        ScrapeQueuePort scrapeQueuePort
) implements Persister<ConfigWithLocalElections> {

    @Override
    public ScrapeResult persist(ConfigWithLocalElections input) {
        final var config = input.seriesConfig();
        final var results = input.results();
        if (results.isEmpty()) {
            log.info("Found no new local election results for {}", config.name());
        } else {
            final var persistedCount = repositoryPort.writeLocalElectionResults(results);
            log.info("Upserted {} local election areas for {}", persistedCount, config.name());
        }
        // The item stays pending for a retry, which only re-fetches the failed files because the
        // md5s of the written results are now stored
        if (input.partialFailure()) {
            log.warn("Some result files of {} failed, leaving it pending", config.name());

            return ScrapeResult.FAILED;
        }
        scrapeQueuePort.markAsDone(config);

        return results.isEmpty() ? ScrapeResult.EMPTY : ScrapeResult.SUCCESS;
    }

}
