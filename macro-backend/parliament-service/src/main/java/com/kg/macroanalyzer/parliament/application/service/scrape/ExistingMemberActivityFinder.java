package com.kg.macroanalyzer.parliament.application.service.scrape;

import com.kg.macroanalyzer.parliament.application.port.out.ConfigWithMemberActivity;
import com.kg.macroanalyzer.parliament.application.port.out.ParliamentRepositoryPort;
import com.kg.macroanalyzer.parliament.domain.MemberOfParliament;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.ExistingFinder;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

// Activity is only fetched for members of the latest terms to keep the number of Riksdagen calls bounded
@Slf4j
public record ExistingMemberActivityFinder(
        ParliamentRepositoryPort repositoryPort,
        int termCount
) implements ExistingFinder<SeriesConfig, ConfigWithMemberActivity> {

    @Override
    public Optional<ConfigWithMemberActivity> findExisting(SeriesConfig seriesConfig) {
        final var members = repositoryPort.readMembersOfParliament();
        if (members.isEmpty()) {
            log.info("No members of parliament stored yet, postponing %s".formatted(seriesConfig.name()));

            return Optional.empty();
        }
        final var terms = repositoryPort.readElections().stream()
                .limit(termCount)
                .toList();
        final var intressentIds = members.stream()
                .filter(m -> terms.stream().anyMatch(t -> m.satInChamberDuring(t.termStart(), t.termEnd())))
                .map(MemberOfParliament::intressent_id)
                .distinct()
                .toList();

        return Optional.of(new ConfigWithMemberActivity(seriesConfig, intressentIds, List.of()));
    }

}
