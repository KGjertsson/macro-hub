package com.kg.macroanalyzer.parliament.application.service.scrape;

import com.kg.macroanalyzer.parliament.application.port.out.ConfigWithMemberOfParliament;
import com.kg.macroanalyzer.parliament.domain.MemberOfParliament;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Scraper;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import com.kg.macroanalyzer.shared.web.WebPort;
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

