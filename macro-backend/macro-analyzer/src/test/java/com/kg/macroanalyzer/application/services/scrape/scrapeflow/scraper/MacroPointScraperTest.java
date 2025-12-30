package com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper;

import com.kg.macroanalyzer.TestSeriesConfigFactory;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMacroPoints;
import com.kg.macroanalyzer.application.services.scrape.ScrapeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MacroPointScraperTest {

    MacroPointScraper scraper;

    @Mock
    ScrapeUtils scrapeUtils;

    @BeforeEach
    void setUp() {
        scraper = new MacroPointScraper(scrapeUtils);
    }

    @Test
    void shouldReturnMacroPoints_whenScrapeSucceeds() throws ScrapeException {
        // given
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();

        // when
        when(scrapeUtils.scrapeNovelItems(seriesConfig, emptyList())).thenReturn(emptyList());
        final var scraped = scraper.scrape(new ConfigWithMacroPoints(seriesConfig, emptyList()));

        // then
        assertTrue(scraped.isPresent());
    }

    @Test
    void shouldReturnEmptyOptional_whenScrapeFails() throws ScrapeException {
        // given
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();

        // when
        when(scrapeUtils.scrapeNovelItems(seriesConfig, emptyList()))
                .thenThrow(new ScrapeException("Scrape failed"));
        final var scraped = scraper.scrape(new ConfigWithMacroPoints(seriesConfig, emptyList()));

        // then
        assertTrue(scraped.isEmpty());
    }

}
