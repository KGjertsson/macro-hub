package com.kg.macroanalyzer.application.services;

import com.kg.macroanalyzer.TestSeriesConfigFactory;
import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.services.scrape.ScrapeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScrapeServiceTest {

    ScrapeService scrapeService;

    @Mock
    DatabasePort databasePort;

    // public List<ScrapeResult> scrapeFromQueue(LocalDateTime timeStamp) {
    // success response
    // empty response
    // failed response
    // one of each response

    @BeforeEach
    public void setUp() {
        final var seriesConfig = TestSeriesConfigFactory.buildTestConfig();
        scrapeService = new ScrapeService(databasePort, seriesConfig);
    }

    @Test
    public void returnEmptyResponse_whenDatabaseResponseIsEmpty() {
        // when
        when(databasePort.getItemsToScrape(any())).thenReturn(emptyList());
        final var response = scrapeService.scrapeFromQueue(null);

        // then
        assertTrue(response.isEmpty());
    }

    @Test
    public void foo() {
        // given
        final var queueItemDate = LocalDateTime.of(2024, 1, 1, 0, 0).toInstant(ZoneOffset.UTC);
        final var databaseResponse = List.of(
                new ScrapeQueueItem(1, "item1", queueItemDate)
        );
        final var inputItemDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        final var macroSeries = MacroSeries.builder()
                .name("name1")
                .macroPoints(List.of())
                .build();

        // when
        when(databasePort.getItemsToScrape(any())).thenReturn(databaseResponse);
        when(databasePort.readMacroSeries())
        final var response = scrapeService.scrapeFromQueue(inputItemDate);

        // then
        assertTrue(response.isEmpty());
    }

}
