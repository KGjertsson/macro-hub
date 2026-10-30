package com.kg.macroanalyzer.scrape.application.service;

import com.kg.macroanalyzer.scrape.application.port.in.ScrapeRun;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.RegisteredScrapeFlow;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.SeriesConfigPreparer;
import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem;
import com.kg.macroanalyzer.scrape.domain.ScrapeResult;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
public class ScrapeServiceTest {

    // The ids are opaque to scrape; the tests use the modules' ids so the routing reads like production
    private static final String MACRO_POINT = "MACRO_POINT";
    private static final String MEMBER_OF_PARLIAMENT = "MEMBER_OF_PARLIAMENT";
    private static final String MEMBER_ACTIVITY = "MEMBER_ACTIVITY";
    private static final String LOCAL_ELECTION = "LOCAL_ELECTION";
    private static final String SCB_REGIONAL = "SCB_REGIONAL";

    private static final List<SeriesConfig> SERIES_CONFIGS = List.of(
            config("test-name", MACRO_POINT),
            config("MembersOfParliament", MEMBER_OF_PARLIAMENT),
            config("MembersOfParliamentActivity", MEMBER_ACTIVITY),
            config("MunicipalElections", LOCAL_ELECTION),
            config("RegionElections", LOCAL_ELECTION),
            config("ParliamentElections", LOCAL_ELECTION),
            config("ScbRegionalStatistics", SCB_REGIONAL)
    );

    ScrapeService scrapeService;

    @Mock
    ScrapeQueuePort scrapeQueuePort;
    @Mock
    RegisteredScrapeFlow macroPointScrapeFlow;
    @Mock
    RegisteredScrapeFlow memberScrapeFlow;
    @Mock
    RegisteredScrapeFlow memberActivityScrapeFlow;
    @Mock
    RegisteredScrapeFlow localElectionScrapeFlow;
    @Mock
    RegisteredScrapeFlow scbRegionalScrapeFlow;

    @BeforeEach
    public void setUp() {
        lenient().when(macroPointScrapeFlow.flowId()).thenReturn(MACRO_POINT);
        lenient().when(memberScrapeFlow.flowId()).thenReturn(MEMBER_OF_PARLIAMENT);
        lenient().when(memberActivityScrapeFlow.flowId()).thenReturn(MEMBER_ACTIVITY);
        lenient().when(localElectionScrapeFlow.flowId()).thenReturn(LOCAL_ELECTION);
        lenient().when(scbRegionalScrapeFlow.flowId()).thenReturn(SCB_REGIONAL);
        scrapeService = scrapeService(allFlows(), SERIES_CONFIGS);
        // Registration reads every flow id; the routing tests only care about run()
        clearInvocations(macroPointScrapeFlow, memberScrapeFlow, memberActivityScrapeFlow, localElectionScrapeFlow, scbRegionalScrapeFlow);
    }

    private List<RegisteredScrapeFlow> allFlows() {
        return List.of(
                macroPointScrapeFlow, memberScrapeFlow, memberActivityScrapeFlow, localElectionScrapeFlow, scbRegionalScrapeFlow);
    }

    private ScrapeService scrapeService(List<RegisteredScrapeFlow> flows, List<SeriesConfig> configs) {
        return new ScrapeService(scrapeQueuePort, flows, configs, new SeriesConfigPreparer(configs));
    }

    private static SeriesConfig config(String name, String scrapeFlow) {
        return SeriesConfig.builder().name(name).scrapeFlow(scrapeFlow).build();
    }

    @Test
    void shouldFailAtStartup_whenAConfigNamesAnUnregisteredScrapeFlow() {
        // given
        final var configs = new ArrayList<>(SERIES_CONFIGS);
        configs.add(config("NewSeries", "NEW_FLOW"));

        // when
        final var exception = assertThrows(IllegalStateException.class, () -> scrapeService(allFlows(), configs));

        // then
        assertTrue(exception.getMessage().contains("NewSeries (scrapeFlow=NEW_FLOW)"), exception.getMessage());
    }

    @Test
    void shouldFailAtStartup_whenAConfigHasNoScrapeFlow() {
        // given
        final var configs = List.of(config("test-name", null));

        // when
        final var exception = assertThrows(IllegalStateException.class, () -> scrapeService(allFlows(), configs));

        // then
        assertTrue(exception.getMessage().contains("test-name (scrapeFlow=null)"), exception.getMessage());
    }

    @Test
    void shouldFailAtStartup_whenAFlowIdHasMoreThanOneScrapeFlow() {
        // given
        final var duplicate = mock(RegisteredScrapeFlow.class);
        when(duplicate.flowId()).thenReturn(LOCAL_ELECTION);
        final var flows = new ArrayList<>(allFlows());
        flows.add(duplicate);

        // when
        final var exception = assertThrows(IllegalStateException.class, () -> scrapeService(flows, SERIES_CONFIGS));

        // then
        assertTrue(exception.getMessage().contains("LOCAL_ELECTION"), exception.getMessage());
    }

    @Test
    void shouldFailAtStartup_whenAScrapeFlowHasNoFlowId() {
        // given
        final var noId = mock(RegisteredScrapeFlow.class);
        when(noId.flowId()).thenReturn(null);
        final var flows = new ArrayList<>(allFlows());
        flows.add(noId);

        // when
        final var exception = assertThrows(IllegalStateException.class, () -> scrapeService(flows, SERIES_CONFIGS));

        // then
        assertTrue(exception.getMessage().contains("has no flow id"), exception.getMessage());
    }

    @Test
    void shouldOnlyWarn_whenARegisteredScrapeFlowHasNoConfig(CapturedOutput output) {
        // given
        final var configs = SERIES_CONFIGS.stream()
                .filter(c -> !c.scrapeFlow().equals(SCB_REGIONAL))
                .toList();

        // when
        assertDoesNotThrow(() -> scrapeService(allFlows(), configs));

        // then
        assertTrue(output.getOut().contains("Scrape flow SCB_REGIONAL is registered but no series config uses it"), output.getOut());
        assertFalse(output.getOut().contains("Scrape flow MACRO_POINT is registered"), output.getOut());
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
        assertEquals(List.of(ScrapeResult.SUCCESS), result);
        verifyNoInteractions(macroPointScrapeFlow, memberScrapeFlow, memberActivityScrapeFlow, localElectionScrapeFlow);
    }

    @Test
    void shouldFailTheItem_whenNoConfigMatchesItsName(CapturedOutput output) {
        // given
        final var now = LocalDateTime.now();
        final var scrapeQueueItem = ScrapeQueueItem.of("RemovedSeries");

        // when
        when(scrapeQueuePort.getItemsToScrape(now)).thenReturn(List.of(scrapeQueueItem));

        final var result = completedResults(scrapeService.scrapeFromQueue(now));

        // then
        assertEquals(List.of(ScrapeResult.FAILED), result);
        assertTrue(output.getOut().contains("No series config named RemovedSeries"), output.getOut());
        verify(scrapeQueuePort, never()).markAsDone(any());
        verifyNoInteractions(macroPointScrapeFlow, memberScrapeFlow, memberActivityScrapeFlow, localElectionScrapeFlow, scbRegionalScrapeFlow);
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
