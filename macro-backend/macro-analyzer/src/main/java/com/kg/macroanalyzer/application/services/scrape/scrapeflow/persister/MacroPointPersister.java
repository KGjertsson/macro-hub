package com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister;

import com.kg.macroanalyzer.application.ports.driven.ConfigWithMacroPoints;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.services.scrape.ScrapeResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record MacroPointPersister(DatabasePort databasePort) implements Persister<ConfigWithMacroPoints> {

    @Override
    public ScrapeResult persist(ConfigWithMacroPoints configWithMacroPoints) {
        final var name = configWithMacroPoints.seriesConfig().name();
        final var config = configWithMacroPoints.seriesConfig();
        databasePort.markAsDone(config);
        if (configWithMacroPoints.macroPoints().isEmpty()) {
            final var msg = "Found no new macro points for %s".formatted(name);
            log.info(msg);

            return ScrapeResult.EMPTY;
        }
        final var persistedCount = databasePort.writeMacroPoints(configWithMacroPoints);
        final var msg = "Persist %s novel macro points for %s".formatted(persistedCount, name);
        log.info(msg);

        return ScrapeResult.SUCCESS;
    }
}
