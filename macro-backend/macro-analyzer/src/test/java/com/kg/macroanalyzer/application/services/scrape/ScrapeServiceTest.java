package com.kg.macroanalyzer.application.services.scrape;

import com.kg.macroanalyzer.TestJsonReader;
import com.kg.macroanalyzer.TestSeriesConfigFactory;
import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueKind;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithLocalElections;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMacroPoints;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMemberActivity;
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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    @Mock
    ScrapeFlow<ScrapeQueueItem, SeriesConfig, ConfigWithMemberActivity> memberActivityScrapeFlow;
    @Mock
    ScrapeFlow<ScrapeQueueItem, SeriesConfig, ConfigWithLocalElections> localElectionScrapeFlow;

    @BeforeEach
    public void setUp() {
        seriesConfigList = TestSeriesConfigFactory.buildTestConfig();
        scrapeService = new ScrapeService(databasePort, macroPointScrapeFlow, memberScrapeFlow, memberActivityScrapeFlow, localElectionScrapeFlow);
        testJsonReader = new TestJsonReader();
    }

    @Test
    void shouldReturnScrapeResults_whenItemsFoundInQueue() {
        // given
        final var now = LocalDateTime.now();
        final var scrapeQueueItem = ScrapeQueueItem.builder()
                .name("test-name")
                .scrapeDate(now.toInstant(ZoneOffset.UTC))
                .scrapeQueueKind(ScrapeQueueKind.MACRO_POINT)
                .build();
        final List<ScrapeQueueItem> items = List.of(scrapeQueueItem);

        // when
        when(databasePort.getItemsToScrape(now)).thenReturn(items);
        when(macroPointScrapeFlow.run(scrapeQueueItem)).thenReturn(ScrapeResult.SUCCESS);

        final var result = scrapeService.scrapeFromQueue(now).orElseThrow();

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

        final var result = scrapeService.scrapeFromQueue(now).orElseThrow();

        // then
        assertTrue(result.isEmpty());
        verify(databasePort).getItemsToScrape(now);
        verifyNoInteractions(macroPointScrapeFlow);
    }

    @Test
    void shouldRouteToMemberScrapeFlow_whenMembersOfParliamentQueued() {
        // given
        final var now = LocalDateTime.now();
        final var scrapeQueueItem = ScrapeQueueItem.of("MembersOfParliament");

        // when
        when(databasePort.getItemsToScrape(now)).thenReturn(List.of(scrapeQueueItem));
        when(memberScrapeFlow.run(scrapeQueueItem)).thenReturn(ScrapeResult.SUCCESS);

        final var result = scrapeService.scrapeFromQueue(now).orElseThrow();

        // then
        assertEquals(List.of(ScrapeResult.SUCCESS), result);
        verify(memberScrapeFlow).run(scrapeQueueItem);
        verifyNoInteractions(macroPointScrapeFlow, memberActivityScrapeFlow);
    }

    @Test
    void shouldRouteToMemberActivityScrapeFlow_whenMemberActivityQueued() {
        // given
        final var now = LocalDateTime.now();
        final var scrapeQueueItem = ScrapeQueueItem.of("MembersOfParliamentActivity");

        // when
        when(databasePort.getItemsToScrape(now)).thenReturn(List.of(scrapeQueueItem));
        when(memberActivityScrapeFlow.run(scrapeQueueItem)).thenReturn(ScrapeResult.SUCCESS);

        final var result = scrapeService.scrapeFromQueue(now).orElseThrow();

        // then
        assertEquals(List.of(ScrapeResult.SUCCESS), result);
        verify(memberActivityScrapeFlow).run(scrapeQueueItem);
        verifyNoInteractions(macroPointScrapeFlow, memberScrapeFlow);
    }

    @Test
    void shouldRouteToLocalElectionScrapeFlow_whenElectionsQueued() {
        // given
        final var now = LocalDateTime.now();
        final var municipal = ScrapeQueueItem.of("MunicipalElections");
        final var region = ScrapeQueueItem.of("RegionElections");

        // when
        when(databasePort.getItemsToScrape(now)).thenReturn(List.of(municipal, region));
        when(localElectionScrapeFlow.run(municipal)).thenReturn(ScrapeResult.SUCCESS);
        when(localElectionScrapeFlow.run(region)).thenReturn(ScrapeResult.EMPTY);

        final var result = scrapeService.scrapeFromQueue(now).orElseThrow();

        // then
        assertEquals(List.of(ScrapeResult.SUCCESS, ScrapeResult.EMPTY), result);
        verifyNoInteractions(macroPointScrapeFlow, memberScrapeFlow, memberActivityScrapeFlow);
    }

    @Test
    void shouldSkipScrape_whenAnotherScrapeIsInProgress() throws Exception {
        // given
        final var now = LocalDateTime.now();
        final var scrapeQueueItem = ScrapeQueueItem.of("MunicipalElections");
        final var firstScrapeStarted = new CountDownLatch(1);
        final var releaseFirstScrape = new CountDownLatch(1);

        // when
        when(databasePort.getItemsToScrape(now)).thenReturn(List.of(scrapeQueueItem));
        when(localElectionScrapeFlow.run(scrapeQueueItem)).thenAnswer(invocation -> {
            firstScrapeStarted.countDown();
            releaseFirstScrape.await(5, TimeUnit.SECONDS);
            return ScrapeResult.SUCCESS;
        });

        final var firstScrape = CompletableFuture.supplyAsync(() -> scrapeService.scrapeFromQueue(now));
        assertTrue(firstScrapeStarted.await(5, TimeUnit.SECONDS));
        final var concurrentResult = scrapeService.scrapeFromQueue(now);
        releaseFirstScrape.countDown();

        // then
        assertEquals(Optional.empty(), concurrentResult);
        assertEquals(Optional.of(List.of(ScrapeResult.SUCCESS)), firstScrape.get(5, TimeUnit.SECONDS));
        verify(databasePort, times(1)).getItemsToScrape(now);
        verify(localElectionScrapeFlow, times(1)).run(scrapeQueueItem);
    }

    @Test
    void shouldAllowNextScrape_whenPreviousScrapeFailed() {
        // given
        final var now = LocalDateTime.now();

        // when
        when(databasePort.getItemsToScrape(now))
                .thenThrow(new RuntimeException("db down"))
                .thenReturn(emptyList());

        // then
        assertThrows(RuntimeException.class, () -> scrapeService.scrapeFromQueue(now));
        assertEquals(Optional.of(emptyList()), scrapeService.scrapeFromQueue(now));
    }

}
