package com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder;

import com.kg.macroanalyzer.application.domain.localelection.LocalElectionType;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithLocalElections;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

// Stored md5s let the scraper skip result files that have not changed since they were last downloaded
@Slf4j
public record ExistingLocalElectionFinder(
        DatabasePort databasePort
) implements ExistingFinder<SeriesConfig, ConfigWithLocalElections> {

    @Override
    public Optional<ConfigWithLocalElections> findExisting(SeriesConfig seriesConfig) {
        final var type = LocalElectionType.fromConfigName(seriesConfig.name());
        if (type.isEmpty()) {
            log.error("No local election type for config %s".formatted(seriesConfig.name()));

            return Optional.empty();
        }

        return Optional.of(ConfigWithLocalElections.builder()
                .seriesConfig(seriesConfig)
                .type(type.get())
                .existingMd5s(databasePort.readLocalElectionSourceMd5s(type.get()))
                .results(List.of())
                .build());
    }

}
