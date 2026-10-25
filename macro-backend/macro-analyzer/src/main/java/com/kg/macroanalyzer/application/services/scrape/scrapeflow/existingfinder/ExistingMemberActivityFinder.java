package com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder;

import com.kg.macroanalyzer.application.domain.parliament.MemberOfParliament;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMemberActivity;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

// Activity is only fetched for members of the latest terms to keep the number of Riksdagen calls bounded
@Slf4j
public record ExistingMemberActivityFinder(
        DatabasePort databasePort,
        int termCount
) implements ExistingFinder<SeriesConfig, ConfigWithMemberActivity> {

    @Override
    public Optional<ConfigWithMemberActivity> findExisting(SeriesConfig seriesConfig) {
        final var members = databasePort.readMembersOfParliament();
        if (members.isEmpty()) {
            log.info("No members of parliament stored yet, postponing %s".formatted(seriesConfig.name()));

            return Optional.empty();
        }
        final var terms = databasePort.readElections().stream()
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
