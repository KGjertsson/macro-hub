package com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder;

import com.kg.macroanalyzer.application.ports.driven.ConfigWithMacroPoints;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;

import java.util.Optional;

public record ExistingMacroPointFinder(
        DatabasePort databasePort
) implements ExistingFinder<SeriesConfig, ConfigWithMacroPoints> {

    @Override
    public Optional<ConfigWithMacroPoints> findExisting(SeriesConfig seriesConfig) {
        final var existing = databasePort.readMacroSeries(seriesConfig)
                .macroPoints();

        return Optional.of(new ConfigWithMacroPoints(seriesConfig, existing));
    }

}
