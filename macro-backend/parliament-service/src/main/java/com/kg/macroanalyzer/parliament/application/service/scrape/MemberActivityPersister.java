package com.kg.macroanalyzer.parliament.application.service.scrape;

import com.kg.macroanalyzer.parliament.application.port.out.ConfigWithMemberActivity;
import com.kg.macroanalyzer.parliament.application.port.out.ParliamentRepositoryPort;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Persister;
import com.kg.macroanalyzer.scrape.domain.ScrapeResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record MemberActivityPersister(ParliamentRepositoryPort repositoryPort, ScrapeQueuePort scrapeQueuePort) implements Persister<ConfigWithMemberActivity> {

    @Override
    public ScrapeResult persist(ConfigWithMemberActivity configWithMemberActivity) {
        final var config = configWithMemberActivity.seriesConfig();
        final var activity = configWithMemberActivity.memberActivity();
        if (activity.isEmpty()) {
            log.info("Found no member activity for {}", config.name());
            scrapeQueuePort.markAsDone(config);

            return ScrapeResult.EMPTY;
        }
        final var persistedCount = repositoryPort.writeMemberActivity(activity);
        scrapeQueuePort.markAsDone(config);
        log.info("Upserted activity for {} members of parliament", persistedCount);

        return ScrapeResult.SUCCESS;
    }

}
