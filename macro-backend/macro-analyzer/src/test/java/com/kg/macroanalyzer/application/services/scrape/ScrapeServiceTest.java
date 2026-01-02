package com.kg.macroanalyzer.application.services.scrape;

import com.kg.macroanalyzer.TestJsonReader;
import com.kg.macroanalyzer.TestSeriesConfigFactory;
import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMacroPoints;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMemberOfParliament;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.ScrapeFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScrapeServiceTest {

    ScrapeService scrapeService;

    TestJsonReader testJsonReader;

    List<SeriesConfig> seriesConfigList;

    @Mock
    DatabasePort databasePort;
    @Mock
    ScrapeFlow<ScrapeQueueItem, SeriesConfig, ConfigWithMacroPoints> macroPointScrapeFlow;
    @Mock
    ScrapeFlow<ScrapeQueueItem, SeriesConfig, ConfigWithMemberOfParliament> memberScrapeFlow;

    @BeforeEach
    public void setUp() {
        seriesConfigList = TestSeriesConfigFactory.buildTestConfig();
        scrapeService = new ScrapeService(databasePort, macroPointScrapeFlow, memberScrapeFlow);
        testJsonReader = new TestJsonReader();
    }

    @Test
    void shouldReturnScrapeResults_whenItemsFoundInQueue() {
        // given
        final var now = LocalDateTime.now();
        final var scrapeQueueItem = ScrapeQueueItem.builder()
                .name("test-name")
                .scrapeDate(now.toInstant(ZoneOffset.UTC))
                .build();
        final List<ScrapeQueueItem> items = List.of(scrapeQueueItem);

        // when
        when(databasePort.getItemsToScrape(now)).thenReturn(items);
        when(macroPointScrapeFlow.run(scrapeQueueItem)).thenReturn(ScrapeResult.SUCCESS);

        final var result = scrapeService.scrapeFromQueue(now);

        // then
        assertEquals(1, result.size());
        assertEquals(ScrapeResult.SUCCESS, result.getFirst());
        verify(databasePort).getItemsToScrape(now);
        verify(macroPointScrapeFlow).run(scrapeQueueItem);
    }

    @Test
    void shouldReturnEmptyList_whenNoItemsFoundInQueue() {
        // given
        final var now = LocalDateTime.now();

        // when
        when(databasePort.getItemsToScrape(now)).thenReturn(emptyList());

        final var result = scrapeService.scrapeFromQueue(now);

        // then
        assertTrue(result.isEmpty());
        verify(databasePort).getItemsToScrape(now);
        verifyNoInteractions(macroPointScrapeFlow);
    }

}
