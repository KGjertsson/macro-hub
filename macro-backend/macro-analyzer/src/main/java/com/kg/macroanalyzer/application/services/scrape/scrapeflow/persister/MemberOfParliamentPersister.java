package com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister;

import com.kg.macroanalyzer.application.ports.driven.ConfigWithMemberOfParliament;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.services.scrape.ScrapeResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record MemberOfParliamentPersister(DatabasePort databasePort) implements Persister<ConfigWithMemberOfParliament> {

    @Override
    public ScrapeResult persist(ConfigWithMemberOfParliament configWithMemberOfParliament) {
        final var config = configWithMemberOfParliament.seriesConfig();
        final var members = configWithMemberOfParliament.membersOfParliament();
        databasePort.markAsDone(config);
        if (members.isEmpty()) {
            log.info("Found no members of parliament for %s".formatted(config.name()));

            return ScrapeResult.EMPTY;
        }
        final var persistedCount = databasePort.writeMembersOfParliament(members);
        log.info("Upserted %s members of parliament for %s".formatted(persistedCount, config.name()));

        return ScrapeResult.SUCCESS;
    }

}
