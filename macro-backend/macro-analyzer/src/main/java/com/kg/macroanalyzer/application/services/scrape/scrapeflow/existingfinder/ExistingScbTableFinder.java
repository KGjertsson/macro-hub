package com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder;

import com.kg.macroanalyzer.application.ports.driven.ConfigWithScbIndicators;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.ExistingFinder;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;

import java.util.List;
import java.util.Optional;
import java.util.Set;

// Stored table stamps let the scraper skip indicators whose SCB tables have not been updated since the last scrape
public record ExistingScbTableFinder(
        DatabasePort databasePort
) implements ExistingFinder<SeriesConfig, ConfigWithScbIndicators> {

    @Override
    public Optional<ConfigWithScbIndicators> findExisting(SeriesConfig seriesConfig) {
        return Optional.of(ConfigWithScbIndicators.builder()
                .seriesConfig(seriesConfig)
                .indicators(databasePort.readScbIndicators())
                .tableStamps(databasePort.readScbTableStamps())
                .indicatorsWithData(databasePort.readScbIndicatorsWithData())
                .results(List.of())
                .failedTables(Set.of())
                .build());
    }

}
