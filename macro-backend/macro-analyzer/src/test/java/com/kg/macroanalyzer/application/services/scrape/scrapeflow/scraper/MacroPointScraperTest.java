package com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper;

import com.kg.macroanalyzer.TestSeriesConfigFactory;
import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMacroPoints;
import com.kg.macroanalyzer.application.ports.driven.WebPort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MacroPointScraperTest {

    MacroPointScraper scraper;

    @Mock
    WebPort<SeriesConfig, Stream<MacroPoint>> webPort;

    @BeforeEach
    void setUp() {
        scraper = new MacroPointScraper(webPort);
    }

    @Test
    void shouldReturnMacroPoints_whenScrapeSucceeds() throws ScrapeException {
        // given
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();

        // when
        when(webPort.fetch(any())).thenReturn(Stream.empty());
        final var scraped = scraper.scrape(new ConfigWithMacroPoints(seriesConfig, emptyList()));

        // then
        assertTrue(scraped.isPresent());
    }

    @Test
    void shouldReturnEmptyOptional_whenScrapeFails() throws ScrapeException {
        // given
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();

        // when
        when(webPort.fetch(any())).thenThrow(new ScrapeException("error"));
        final var scraped = scraper.scrape(new ConfigWithMacroPoints(seriesConfig, emptyList()));

        // then
        assertTrue(scraped.isEmpty());
    }

}
