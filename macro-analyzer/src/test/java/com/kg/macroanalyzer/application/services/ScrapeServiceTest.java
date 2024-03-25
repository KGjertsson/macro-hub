package com.kg.macroanalyzer.application.services;

import com.kg.macroanalyzer.TestJsonReader;
import com.kg.macroanalyzer.TestSeriesConfigFactory;
import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMacroPoints;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.application.services.scrape.ScrapeAdaptor;
import com.kg.macroanalyzer.application.services.scrape.ScrapeResult;
import com.kg.macroanalyzer.application.services.scrape.ScrapeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScrapeServiceTest {

    ScrapeService scrapeService;

    TestJsonReader testJsonReader;

    List<SeriesConfig> seriesConfigList;

    @Mock
    DatabasePort databasePort;
    @Mock
    ScrapeAdaptor scrapeAdaptor;

    @BeforeEach
    public void setUp() {
        seriesConfigList = TestSeriesConfigFactory.buildTestConfig();
        scrapeService = new ScrapeService(databasePort, scrapeAdaptor, seriesConfigList);
        testJsonReader = new TestJsonReader();
    }

    @Test
    public void returnEmptyResponse_whenDatabaseResponseIsEmpty() {
        // when
        when(databasePort.getItemsToScrape(any())).thenReturn(emptyList());
        final var response = scrapeService.scrapeFromQueue(null);

        // then
        assertTrue(response.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"json/single_macro_series.json"})
    public void returnSuccessfulResponse_whenNovelScrapedExists(String resource) throws ScrapeException {
        // given
        final var datasetName = "macro_series_1";
        final var macroSeriesList = testJsonReader.readMacroSeriesList(resource);
        final var queueItemDate = LocalDateTime.of(2024, 1, 1, 0, 0)
                .toInstant(ZoneOffset.UTC);
        final var databaseResponse = List.of(
                new ScrapeQueueItem(1, datasetName, queueItemDate)
        );
        final var inputItemDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        final var macroSeries = macroSeriesList.stream()
                .filter(c -> c.name().equals(datasetName))
                .toList()
                .getFirst();
        final var seriesConfig = seriesConfigList.stream()
                .filter(c -> c.name().equals(macroSeries.name()))
                .toList()
                .getFirst();
        final var expectedWrite = ConfigWithMacroPoints.builder()
                .seriesConfig(seriesConfig)
                .macroPoints(macroSeries.macroPoints())
                .build();
        final var emptySeries = MacroSeries.builder()
                .name(datasetName)
                .macroPoints(emptyList())
                .build();

        // when
        when(databasePort.getItemsToScrape(any())).thenReturn(databaseResponse);
        when(databasePort.readMacroSeries(any(SeriesConfig.class))).thenReturn(emptySeries);
        when(databasePort.writeMacroPoints(any())).thenReturn(2);
        when(scrapeAdaptor.scrapeNovelItems(any(), any())).thenReturn(macroSeries.macroPoints());

        final var response = scrapeService.scrapeFromQueue(inputItemDate);

        // then
        final var successResponse = response.stream().filter(ScrapeResult::isSuccess).count();
        final var emptyResponse = response.stream().filter(ScrapeResult::isEmpty).count();
        final var errorResponse = response.stream().filter(ScrapeResult::isFailed).count();

        assertEquals(1, successResponse);
        assertEquals(0, emptyResponse);
        assertEquals(0, errorResponse);

        verify(databasePort).writeMacroPoints(expectedWrite);
    }

    @ParameterizedTest
    @ValueSource(strings = {"json/single_macro_series.json"})
    public void returnEmptyResponse_whenNoNovelScrapedExists(String resource) throws ScrapeException {
        // given
        final var datasetName = "macro_series_1";
        final var macroSeriesList = testJsonReader.readMacroSeriesList(resource);
        final var queueItemDate = LocalDateTime.of(2024, 1, 1, 0, 0)
                .toInstant(ZoneOffset.UTC);
        final var databaseResponse = List.of(
                new ScrapeQueueItem(1, datasetName, queueItemDate)
        );
        final var inputItemDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        final var persistedSeries = macroSeriesList.stream()
                .filter(c -> c.name().equals(datasetName))
                .toList()
                .getFirst();

        // when
        when(databasePort.getItemsToScrape(any())).thenReturn(databaseResponse);
        when(databasePort.readMacroSeries(any(SeriesConfig.class))).thenReturn(persistedSeries);
        when(scrapeAdaptor.scrapeNovelItems(any(), any())).thenReturn(emptyList());

        final var response = scrapeService.scrapeFromQueue(inputItemDate);

        // then
        final var successResponse = response.stream().filter(ScrapeResult::isSuccess).count();
        final var emptyResponse = response.stream().filter(ScrapeResult::isEmpty).count();
        final var errorResponse = response.stream().filter(ScrapeResult::isFailed).count();

        assertEquals(0, successResponse);
        assertEquals(1, emptyResponse);
        assertEquals(0, errorResponse);

        verify(databasePort, never()).writeMacroPoints(any());
    }

    @Test
    public void returnFailedResponse_whenNovelScrapedExists() throws ScrapeException {
        // given
        final var datasetName = "macro_series_1";
        final var inputItemDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        final var queueItemDate = LocalDateTime.of(2024, 1, 1, 0, 0)
                .toInstant(ZoneOffset.UTC);
        final var databaseResponse = List.of(
                new ScrapeQueueItem(1, datasetName, queueItemDate)
        );
        final var emptySeries = MacroSeries.builder()
                .name(datasetName)
                .macroPoints(emptyList())
                .build();
        final var scrapeException = new ScrapeException("test scrape exception");

        // when
        when(databasePort.getItemsToScrape(any())).thenReturn(databaseResponse);
        when(databasePort.readMacroSeries(any(SeriesConfig.class))).thenReturn(emptySeries);
        when(scrapeAdaptor.scrapeNovelItems(any(), any())).thenThrow(scrapeException);

        final var response = scrapeService.scrapeFromQueue(inputItemDate);

        // then
        final var successResponse = response.stream().filter(ScrapeResult::isSuccess).count();
        final var emptyResponse = response.stream().filter(ScrapeResult::isEmpty).count();
        final var errorResponse = response.stream().filter(ScrapeResult::isFailed).count();

        assertEquals(0, successResponse);
        assertEquals(0, emptyResponse);
        assertEquals(1, errorResponse);

        verify(databasePort, never()).writeMacroPoints(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"json/multiple_macro_series.json"})
    public void scrapeWithMultipleResults_eachHandledIndividually(String resource) throws ScrapeException {
        // given
        final var macroSeriesList = testJsonReader.readMacroSeriesList(resource);

        final var datasetName1 = "macro_series_1"; // success
        final var datasetName2 = "macro_series_2"; // empty
        final var datasetName3 = "macro_series_3"; // fail

        final var inputItemDate = LocalDateTime.now();
        final var queueItemDate = inputItemDate.toInstant(ZoneOffset.UTC);

        final var scrapeQueueItem1 = new ScrapeQueueItem(1, datasetName1, queueItemDate);
        final var scrapeQueueItem2 = new ScrapeQueueItem(2, datasetName2, queueItemDate);
        final var scrapeQueueItem3 = new ScrapeQueueItem(3, datasetName3, queueItemDate);

        final var dbResponse = List.of(scrapeQueueItem1, scrapeQueueItem2, scrapeQueueItem3);
        final var config1 = seriesConfigList.stream()
                .filter(c -> c.name().equals(datasetName1))
                .toList().getFirst();
        final var config2 = seriesConfigList.stream()
                .filter(c -> c.name().equals(datasetName2))
                .toList().getFirst();
        final var config3 = seriesConfigList.stream()
                .filter(c -> c.name().equals(datasetName3))
                .toList().getFirst();

        final var macroSeries1 = macroSeriesList.stream()
                .filter(s -> s.name().equals(datasetName1))
                .toList().getFirst();
        final var macroSeries2 = macroSeriesList.stream()
                .filter(s -> s.name().equals(datasetName2))
                .map(MacroSeries::toBuilder)
                .map(m -> m.macroPoints(emptyList()))
                .map(MacroSeries.MacroSeriesBuilder::build)
                .toList().getFirst();
        final var macroSeries3 = macroSeriesList.stream()
                .filter(s -> s.name().equals(datasetName3))
                .toList().getFirst();

        // when
        when(databasePort.getItemsToScrape(any())).thenReturn(dbResponse);
        when(databasePort.readMacroSeries(config1)).thenReturn(macroSeries1);
        when(databasePort.readMacroSeries(config2)).thenReturn(macroSeries2);
        when(databasePort.readMacroSeries(config3)).thenReturn(macroSeries3);

        final var scrapeException = new ScrapeException("test scrape exception");
        when(scrapeAdaptor.scrapeNovelItems(eq(config1), any())).thenReturn(macroSeries1.macroPoints());
        when(scrapeAdaptor.scrapeNovelItems(eq(config2), any())).thenReturn(macroSeries2.macroPoints());
        when(scrapeAdaptor.scrapeNovelItems(eq(config3), any())).thenThrow(scrapeException);

        when(databasePort.writeMacroPoints(any())).thenReturn(1);

        final var response = scrapeService.scrapeFromQueue(inputItemDate);

        // then
        final var successResponse = response.stream().filter(ScrapeResult::isSuccess).count();
        final var emptyResponse = response.stream().filter(ScrapeResult::isEmpty).count();
        final var errorResponse = response.stream().filter(ScrapeResult::isFailed).count();

        assertEquals(1, successResponse);
        assertEquals(1, emptyResponse);
        assertEquals(1, errorResponse);

        verify(databasePort).writeMacroPoints(any());
    }

}
