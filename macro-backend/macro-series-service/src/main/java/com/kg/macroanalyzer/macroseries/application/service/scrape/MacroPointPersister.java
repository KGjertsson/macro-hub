package com.kg.macroanalyzer.macroseries.application.service.scrape;

import com.kg.macroanalyzer.macroseries.application.port.out.ConfigWithMacroPoints;
import com.kg.macroanalyzer.macroseries.application.port.out.MacroSeriesRepositoryPort;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Persister;
import com.kg.macroanalyzer.scrape.domain.ScrapeResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record MacroPointPersister(
        MacroSeriesRepositoryPort repositoryPort,
        ScrapeQueuePort scrapeQueuePort
) implements Persister<ConfigWithMacroPoints> {

    @Override
    public ScrapeResult persist(ConfigWithMacroPoints configWithMacroPoints) {
        final var name = configWithMacroPoints.seriesConfig().name();
        final var config = configWithMacroPoints.seriesConfig();
        if (configWithMacroPoints.macroPoints().isEmpty()) {
            log.info("Found no new or revised macro points for {}", name);
            scrapeQueuePort.markAsDone(config);

            return ScrapeResult.EMPTY;
        }
        final var persistedCount = repositoryPort.writeMacroPoints(configWithMacroPoints);
        scrapeQueuePort.markAsDone(config);
        log.info("Wrote {} new or revised macro points for {}", persistedCount, name);

        return ScrapeResult.SUCCESS;
    }
}
