package com.kg.macroanalyzer.scrape.application.service;

import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.KindedScrapeFlow;
import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem;
import com.kg.macroanalyzer.scrape.domain.ScrapeQueueKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScrapeServiceTest {

    ScrapeService scrapeService;

    @Mock
    ScrapeQueuePort scrapeQueuePort;
    @Mock
    KindedScrapeFlow macroPointScrapeFlow;
    @Mock
    KindedScrapeFlow memberScrapeFlow;
    @Mock
    KindedScrapeFlow memberActivityScrapeFlow;
    @Mock
    KindedScrapeFlow localElectionScrapeFlow;
    @Mock
    KindedScrapeFlow scbRegionalScrapeFlow;

    @BeforeEach
    public void setUp() {
        lenient().when(macroPointScrapeFlow.kind()).thenReturn(ScrapeQueueKind.MACRO_POINT);
        lenient().when(memberScrapeFlow.kind()).thenReturn(ScrapeQueueKind.MEMBER_OF_PARLIAMENT);
        lenient().when(memberActivityScrapeFlow.kind()).thenReturn(ScrapeQueueKind.MEMBER_ACTIVITY);
        lenient().when(localElectionScrapeFlow.kind()).thenReturn(ScrapeQueueKind.LOCAL_ELECTION);
        lenient().when(scbRegionalScrapeFlow.kind()).thenReturn(ScrapeQueueKind.SCB_REGIONAL);
        scrapeService = new ScrapeService(scrapeQueuePort, allFlows());
        // Registration reads every kind; the routing tests only care about run()
        clearInvocations(macroPointScrapeFlow, memberScrapeFlow, memberActivityScrapeFlow, localElectionScrapeFlow, scbRegionalScrapeFlow);
    }

    private List<KindedScrapeFlow> allFlows() {
        return List.of(
                macroPointScrapeFlow, memberScrapeFlow, memberActivityScrapeFlow, localElectionScrapeFlow, scbRegionalScrapeFlow);
    }

    @Test
    void shouldFailAtStartup_whenAKindHasNoScrapeFlow() {
        // given
        final var flows = List.of(macroPointScrapeFlow, memberScrapeFlow, memberActivityScrapeFlow, localElectionScrapeFlow);

        // when
        final var exception = assertThrows(IllegalStateException.class, () -> new ScrapeService(scrapeQueuePort, flows));

        // then
        assertTrue(exception.getMessage().contains("SCB_REGIONAL"), exception.getMessage());
    }

    @Test
    void shouldFailAtStartup_whenAKindHasMoreThanOneScrapeFlow() {
        // given
        final var duplicate = mock(KindedScrapeFlow.class);
        when(duplicate.kind()).thenReturn(ScrapeQueueKind.LOCAL_ELECTION);
        final var flows = new ArrayList<>(allFlows());
        flows.add(duplicate);

        // when
        final var exception = assertThrows(IllegalStateException.class, () -> new ScrapeService(scrapeQueuePort, flows));

        // then
        assertTrue(exception.getMessage().contains("LOCAL_ELECTION"), exception.getMessage());
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
        when(scrapeQueuePort.getItemsToScrape(now)).thenReturn(items);
        when(macroPointScrapeFlow.run(scrapeQueueItem)).thenReturn(ScrapeResult.SUCCESS);

        final var result = completedResults(scrapeService.scrapeFromQueue(now));

        // then
        assertEquals(1, result.size());
        assertEquals(ScrapeResult.SUCCESS, result.getFirst());
        verify(scrapeQueuePort).getItemsToScrape(now);
        verify(macroPointScrapeFlow).run(scrapeQueueItem);
    }

    @Test
    void shouldReturnEmptyList_whenNoItemsFoundInQueue() {
        // given
        final var now = LocalDateTime.now();

        // when
        when(scrapeQueuePort.getItemsToScrape(now)).thenReturn(emptyList());

        final var result = completedResults(scrapeService.scrapeFromQueue(now));

        // then
        assertTrue(result.isEmpty());
        verify(scrapeQueuePort).getItemsToScrape(now);
        verifyNoInteractions(macroPointScrapeFlow);
    }

    @Test
    void shouldRouteToMemberScrapeFlow_whenMembersOfParliamentQueued() {
        // given
        final var now = LocalDateTime.now();
        final var scrapeQueueItem = ScrapeQueueItem.of("MembersOfParliament");

        // when
        when(scrapeQueuePort.getItemsToScrape(now)).thenReturn(List.of(scrapeQueueItem));
        when(memberScrapeFlow.run(scrapeQueueItem)).thenReturn(ScrapeResult.SUCCESS);

        final var result = completedResults(scrapeService.scrapeFromQueue(now));

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
        when(scrapeQueuePort.getItemsToScrape(now)).thenReturn(List.of(scrapeQueueItem));
        when(memberActivityScrapeFlow.run(scrapeQueueItem)).thenReturn(ScrapeResult.SUCCESS);

        final var result = completedResults(scrapeService.scrapeFromQueue(now));

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
        final var parliament = ScrapeQueueItem.of("ParliamentElections");

        // when
        when(scrapeQueuePort.getItemsToScrape(now)).thenReturn(List.of(municipal, region, parliament));
        when(localElectionScrapeFlow.run(municipal)).thenReturn(ScrapeResult.SUCCESS);
        when(localElectionScrapeFlow.run(region)).thenReturn(ScrapeResult.EMPTY);
        when(localElectionScrapeFlow.run(parliament)).thenReturn(ScrapeResult.SUCCESS);

        final var result = completedResults(scrapeService.scrapeFromQueue(now));

        // then
        assertEquals(List.of(ScrapeResult.SUCCESS, ScrapeResult.EMPTY, ScrapeResult.SUCCESS), result);
        verifyNoInteractions(macroPointScrapeFlow, memberScrapeFlow, memberActivityScrapeFlow);
    }

    @Test
    void shouldRouteToScbRegionalScrapeFlow_whenScbStatisticsQueued() {
        // given
        final var now = LocalDateTime.now();
        final var scrapeQueueItem = ScrapeQueueItem.of("ScbRegionalStatistics");

        // when
        when(scrapeQueuePort.getItemsToScrape(now)).thenReturn(List.of(scrapeQueueItem));
        when(scbRegionalScrapeFlow.run(scrapeQueueItem)).thenReturn(ScrapeResult.SUCCESS);

        final var result = completedResults(scrapeService.scrapeFromQueue(now));

        // then
        assertEquals(ScrapeQueueKind.SCB_REGIONAL, scrapeQueueItem.scrapeQueueKind());
        assertEquals(List.of(ScrapeResult.SUCCESS), result);
        verifyNoInteractions(macroPointScrapeFlow, memberScrapeFlow, memberActivityScrapeFlow, localElectionScrapeFlow);
    }

    @Test
    void shouldSkipScrape_whenAnotherScrapeIsInProgress() throws Exception {
        // given
        final var now = LocalDateTime.now();
        final var scrapeQueueItem = ScrapeQueueItem.of("MunicipalElections");
        final var firstScrapeStarted = new CountDownLatch(1);
        final var releaseFirstScrape = new CountDownLatch(1);

        // when
        when(scrapeQueuePort.getItemsToScrape(now)).thenReturn(List.of(scrapeQueueItem));
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
        assertEquals(new ScrapeRun.AlreadyRunning(), concurrentResult);
        assertEquals(new ScrapeRun.Completed(List.of(ScrapeResult.SUCCESS)), firstScrape.get(5, TimeUnit.SECONDS));
        verify(scrapeQueuePort, times(1)).getItemsToScrape(now);
        verify(localElectionScrapeFlow, times(1)).run(scrapeQueueItem);
    }

    @Test
    void shouldAllowNextScrape_whenPreviousScrapeFailed() {
        // given
        final var now = LocalDateTime.now();

        // when
        when(scrapeQueuePort.getItemsToScrape(now))
                .thenThrow(new RuntimeException("db down"))
                .thenReturn(emptyList());

        // then
        assertThrows(RuntimeException.class, () -> scrapeService.scrapeFromQueue(now));
        assertEquals(new ScrapeRun.Completed(emptyList()), scrapeService.scrapeFromQueue(now));
    }

    private static List<ScrapeResult> completedResults(ScrapeRun run) {
        return assertInstanceOf(ScrapeRun.Completed.class, run).results();
    }

}
