package com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper;

import com.kg.macroanalyzer.application.domain.parliament.MemberActivity;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMemberActivity;
import com.kg.macroanalyzer.application.ports.driven.MemberActivityRequest;
import com.kg.macroanalyzer.application.ports.driven.WebPort;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
public record MemberActivityScraper(
        WebPort<MemberActivityRequest, MemberActivity> webPort,
        long delayBetweenMembersMs
) implements Scraper<ConfigWithMemberActivity> {

    @Override
    public Optional<ConfigWithMemberActivity> scrape(ConfigWithMemberActivity configWithMemberActivity) {
        final var baseUrl = configWithMemberActivity.seriesConfig().scrapeUrl();
        final var ids = configWithMemberActivity.intressentIds();
        final var activityList = new ArrayList<MemberActivity>();
        log.info("Fetching activity for %d members of parliament".formatted(ids.size()));

        for (final var id : ids) {
            try {
                activityList.add(webPort.fetch(new MemberActivityRequest(baseUrl, id)));
            } catch (ScrapeException e) {
                log.error("Caught ScrapeException for member=%s: %s".formatted(id, e.getMessage()));
            }
            pause();
        }
        if (activityList.isEmpty() && !ids.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(configWithMemberActivity.toBuilder()
                .memberActivity(activityList)
                .build());
    }

    // Keeps the scrape polite towards Riksdagen's open data service
    private void pause() {
        try {
            Thread.sleep(delayBetweenMembersMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
