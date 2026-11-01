package com.kg.macroanalyzer.macroseries.application.service.scrape;

import com.kg.macroanalyzer.macroseries.application.port.out.ConfigWithMacroPoints;
import com.kg.macroanalyzer.macroseries.application.port.out.MacroSeriesRepositoryPort;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.ExistingFinder;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;

import java.util.Optional;

public record ExistingMacroPointFinder(
        MacroSeriesRepositoryPort repositoryPort
) implements ExistingFinder<SeriesConfig, ConfigWithMacroPoints> {

    @Override
    public Optional<ConfigWithMacroPoints> findExisting(SeriesConfig seriesConfig) {
        final var existing = repositoryPort.readMacroSeries(seriesConfig)
                .macroPoints();

        return Optional.of(new ConfigWithMacroPoints(seriesConfig, existing));
    }

}
