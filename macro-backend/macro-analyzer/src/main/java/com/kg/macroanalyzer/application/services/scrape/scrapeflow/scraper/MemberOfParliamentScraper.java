package com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper;

import com.kg.macroanalyzer.application.domain.parliament.MemberOfParliament;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMemberOfParliament;
import com.kg.macroanalyzer.application.ports.driven.WebPort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public record MemberOfParliamentScraper(
        WebPort<SeriesConfig, Stream<MemberOfParliament>> webPort
) implements Scraper<ConfigWithMemberOfParliament> {

    @Override
    public Optional<ConfigWithMemberOfParliament> scrape(
            ConfigWithMemberOfParliament configWithMemberOfParliament
    ) {
        try {
            final var config = configWithMemberOfParliament.seriesConfig();
            final var scrapedMembersOfParliament = webPort.fetch(config).toList();

            return Optional.ofNullable(
                    configWithMemberOfParliament.toBuilder()
                            .membersOfParliament(scrapedMembersOfParliament)
                            .build()
            );
        } catch (ScrapeException scrapeException) {
            final var msg = "Caught ScrapeException for config=%s";
            final var formattedMsg = String.format(msg, configWithMemberOfParliament.seriesConfig());
            log.error(formattedMsg);

            return Optional.empty();
        }
    }
}

