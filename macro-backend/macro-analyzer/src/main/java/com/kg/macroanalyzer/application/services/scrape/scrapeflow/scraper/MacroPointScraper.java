package com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper;

import com.kg.macroanalyzer.adaptors.webadaptorflow.WebAdaptorFlowFactory;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMacroPoints;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public record MacroPointScraper(
        WebAdaptorFlowFactory webAdaptorFlowFactory
) implements Scraper<ConfigWithMacroPoints> {

    @Override
    public Optional<ConfigWithMacroPoints> scrape(ConfigWithMacroPoints configWithMacroPoints) {
        try {
            final var config = configWithMacroPoints.seriesConfig();
            final var existingMacroPoints = configWithMacroPoints.macroPoints();

            final var scrapeAdaptorFlow = webAdaptorFlowFactory.build(config);
            final var scrapedMacroPoints = scrapeAdaptorFlow.fetch(config);

            final var novelScraped = scrapedMacroPoints
                    .filter(i -> !existingMacroPoints.contains(i))
                    .toList();

            return Optional.ofNullable(
                    configWithMacroPoints.toBuilder()
                            .macroPoints(novelScraped)
                            .build()
            );
        } catch (ScrapeException scrapeException) {
            final var msg = "Caught ScrapeException for config=%s";
            final var formattedMsg = String.format(msg, configWithMacroPoints.seriesConfig());
            log.error(formattedMsg);

            return Optional.empty();
        }
    }

}
