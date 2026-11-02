package com.kg.macroanalyzer.macroseries.application.service.scrape;

import com.kg.macroanalyzer.TestSeriesConfigFactory;
import com.kg.macroanalyzer.macroseries.application.port.out.ConfigWithMacroPoints;
import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import com.kg.macroanalyzer.shared.web.WebPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void shouldKeepOnlyNewAndRevisedPoints() throws ScrapeException {
        // given
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();
        final var unchanged = MacroPoint.builder().date(LocalDate.of(2020, 1, 1)).value(1.0).build();
        final var stored = MacroPoint.builder().date(LocalDate.of(2020, 1, 2)).value(2.0).build();
        final var revised = stored.toBuilder().value(3.0).build();
        final var added = MacroPoint.builder().date(LocalDate.of(2020, 1, 3)).value(4.0).build();

        // when
        when(webPort.fetch(any())).thenReturn(Stream.of(unchanged, revised, added));
        final var scraped = scraper.scrape(new ConfigWithMacroPoints(seriesConfig, List.of(unchanged, stored)));

        // then
        assertEquals(List.of(revised, added), scraped.orElseThrow().macroPoints());
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
