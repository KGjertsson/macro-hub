package com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister;

import com.kg.macroanalyzer.application.ports.driven.ConfigWithMemberActivity;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.application.service.ScrapeResult;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Persister;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record MemberActivityPersister(DatabasePort databasePort, ScrapeQueuePort scrapeQueuePort) implements Persister<ConfigWithMemberActivity> {

    @Override
    public ScrapeResult persist(ConfigWithMemberActivity configWithMemberActivity) {
        final var config = configWithMemberActivity.seriesConfig();
        final var activity = configWithMemberActivity.memberActivity();
        scrapeQueuePort.markAsDone(config);
        if (activity.isEmpty()) {
            log.info("Found no member activity for %s".formatted(config.name()));

            return ScrapeResult.EMPTY;
        }
        final var persistedCount = databasePort.writeMemberActivity(activity);
        log.info("Upserted activity for %s members of parliament".formatted(persistedCount));

        return ScrapeResult.SUCCESS;
    }

}
