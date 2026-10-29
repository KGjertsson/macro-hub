package com.kg.macroanalyzer.macroseries.application.service.scrape;

import com.kg.macroanalyzer.macroseries.application.port.out.ConfigWithMacroPoints;
import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.Scraper;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import com.kg.macroanalyzer.shared.web.WebPort;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public record MacroPointScraper(
        WebPort<SeriesConfig, Stream<MacroPoint>> webPort
) implements Scraper<ConfigWithMacroPoints> {

    @Override
    public Optional<ConfigWithMacroPoints> scrape(ConfigWithMacroPoints configWithMacroPoints) {
        try {
            final var config = configWithMacroPoints.seriesConfig();
            final var existingMacroPoints = configWithMacroPoints.macroPoints();
            final var scrapedMacroPoints = webPort.fetch(config);
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
