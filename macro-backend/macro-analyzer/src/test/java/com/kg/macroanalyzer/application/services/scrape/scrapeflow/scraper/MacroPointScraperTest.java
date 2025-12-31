package com.kg.macroanalyzer.application.services.scrape.scrapeflow.scraper;

import com.kg.macroanalyzer.TestSeriesConfigFactory;
import com.kg.macroanalyzer.adaptors.webadaptorflow.WebAdaptorFlowFactory;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMacroPoints;
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
    WebAdaptorFlowFactory webAdaptorFlowFactory;

    @BeforeEach
    void setUp() {
        scraper = new MacroPointScraper(webAdaptorFlowFactory);
    }

//    @Test
//    void shouldReturnMacroPoints_whenScrapeSucceeds() {
//        // given
//        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();
//
//        // when
//        when(webAdaptorFlowFactory.build(seriesConfig)).thenReturn(null); // TODO
//        final var scraped = scraper.scrape(new ConfigWithMacroPoints(seriesConfig, emptyList()));
//
//        // then
//        assertTrue(scraped.isPresent());
//    }
//
//    @Test
//    void shouldReturnEmptyOptional_whenScrapeFails() {
//        // given
//        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig().getFirst();
//
//        // when
//        when(webAdaptorFlowFactory.build(seriesConfig)).thenReturn(null); // TODO
//        final var scraped = scraper.scrape(new ConfigWithMacroPoints(seriesConfig, emptyList()));
//
//        // then
//        assertTrue(scraped.isEmpty());
//    }

}
