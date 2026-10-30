package com.kg.macroanalyzer.parliament.application.service.scrape;

import com.kg.macroanalyzer.parliament.application.port.out.ConfigWithMemberOfParliament;
import com.kg.macroanalyzer.parliament.application.port.out.ParliamentRepositoryPort;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Persister;
import com.kg.macroanalyzer.scrape.domain.ScrapeResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record MemberOfParliamentPersister(ParliamentRepositoryPort repositoryPort, ScrapeQueuePort scrapeQueuePort) implements Persister<ConfigWithMemberOfParliament> {

    @Override
    public ScrapeResult persist(ConfigWithMemberOfParliament configWithMemberOfParliament) {
        final var config = configWithMemberOfParliament.seriesConfig();
        final var members = configWithMemberOfParliament.membersOfParliament();
        scrapeQueuePort.markAsDone(config);
        if (members.isEmpty()) {
            log.info("Found no members of parliament for %s".formatted(config.name()));

            return ScrapeResult.EMPTY;
        }
        final var persistedCount = repositoryPort.writeMembersOfParliament(members);
        log.info("Upserted %s members of parliament for %s".formatted(persistedCount, config.name()));

        return ScrapeResult.SUCCESS;
    }

}
