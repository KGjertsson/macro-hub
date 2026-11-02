package com.kg.macroanalyzer.macroseries.application.service.scrape;

import com.kg.macroanalyzer.macroseries.application.port.out.ConfigWithMacroPoints;
import com.kg.macroanalyzer.macroseries.application.port.out.MacroSeriesRepositoryPort;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Persister;
import com.kg.macroanalyzer.scrape.domain.ScrapeResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record MacroPointPersister(MacroSeriesRepositoryPort repositoryPort, ScrapeQueuePort scrapeQueuePort) implements Persister<ConfigWithMacroPoints> {

    @Override
    public ScrapeResult persist(ConfigWithMacroPoints configWithMacroPoints) {
        final var name = configWithMacroPoints.seriesConfig().name();
        final var config = configWithMacroPoints.seriesConfig();
        if (configWithMacroPoints.macroPoints().isEmpty()) {
            final var msg = "Found no new or revised macro points for %s".formatted(name);
            log.info(msg);
            scrapeQueuePort.markAsDone(config);

            return ScrapeResult.EMPTY;
        }
        final var persistedCount = repositoryPort.writeMacroPoints(configWithMacroPoints);
        scrapeQueuePort.markAsDone(config);
        final var msg = "Wrote %s new or revised macro points for %s".formatted(persistedCount, name);
        log.info(msg);

        return ScrapeResult.SUCCESS;
    }
}
