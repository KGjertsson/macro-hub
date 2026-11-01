package com.kg.macroanalyzer.regionalstatistics.application.service.scrape;

import com.kg.macroanalyzer.regionalstatistics.application.port.out.ConfigWithScbIndicators;
import com.kg.macroanalyzer.regionalstatistics.application.port.out.RegionalStatisticsRepositoryPort;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.ExistingFinder;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;

import java.util.List;
import java.util.Optional;
import java.util.Set;

// Stored table stamps let the scraper skip indicators whose SCB tables have not been updated since the last scrape
public record ExistingScbTableFinder(
        RegionalStatisticsRepositoryPort repositoryPort
) implements ExistingFinder<SeriesConfig, ConfigWithScbIndicators> {

    @Override
    public Optional<ConfigWithScbIndicators> findExisting(SeriesConfig seriesConfig) {
        return Optional.of(ConfigWithScbIndicators.builder()
                .seriesConfig(seriesConfig)
                .indicators(repositoryPort.readScbIndicators())
                .tableStamps(repositoryPort.readScbTableStamps())
                .indicatorsWithData(repositoryPort.readScbIndicatorsWithData())
                .results(List.of())
                .failedTables(Set.of())
                .build());
    }

}
