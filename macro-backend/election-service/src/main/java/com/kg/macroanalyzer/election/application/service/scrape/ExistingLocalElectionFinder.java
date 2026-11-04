package com.kg.macroanalyzer.election.application.service.scrape;

import com.kg.macroanalyzer.election.application.port.out.ConfigWithLocalElections;
import com.kg.macroanalyzer.election.application.port.out.ElectionRepositoryPort;
import com.kg.macroanalyzer.election.domain.LocalElectionType;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.ExistingFinder;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public record ExistingLocalElectionFinder(
        ElectionRepositoryPort repositoryPort
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
                .existingMd5s(repositoryPort.readLocalElectionSourceMd5s(type.get()))
                .results(List.of())
                .build());
    }

}
