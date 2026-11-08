package com.kg.macroanalyzer.scrape.adaptors.metrics;

import com.kg.macroanalyzer.scrape.application.port.out.ScrapeMetricsPort;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.domain.ScrapeResult;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.distribution.CountAtBucket;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScrapeMetricsMicrometerAdaptorTest {

    private static final Instant START = Instant.parse("2026-11-09T22:00:00Z");
    private static final String FLOW = "MACRO_POINT";

    final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    final AtomicReference<Instant> now = new AtomicReference<>(START);

    @Mock
    ScrapeQueuePort scrapeQueuePort;

    ScrapeMetricsMicrometerAdaptor adaptor;

    @BeforeEach
    void setUp() {
        final InstantSource clock = now::get;
        adaptor = new ScrapeMetricsMicrometerAdaptor(registry, scrapeQueuePort, clock);
    }

    @Test
    void shouldStartEveryFlowAtZero_beforeAnyItemIsProcessed() {
        // when
        adaptor.registerFlows(List.of(FLOW));

        // then
        assertEquals(0, itemCount(FLOW, "success"));
        assertEquals(0, itemCount(FLOW, "failed"));
        assertEquals(0, itemCount(ScrapeMetricsPort.UNKNOWN_FLOW, "failed"));
    }

    @Test
    void shouldCountSuccess_underSuccess() {
        // when
        adaptor.recordItem(FLOW, ScrapeResult.SUCCESS, Duration.ofSeconds(1));

        // then
        assertEquals(1, itemCount(FLOW, "success"));
    }

    @Test
    void shouldCountEmpty_underSuccess() {
        // when
        adaptor.recordItem(FLOW, ScrapeResult.EMPTY, Duration.ofSeconds(1));

        // then
        assertEquals(1, itemCount(FLOW, "success"));
    }

    @Test
    void shouldCountFailed_underFailed() {
        // when
        adaptor.recordItem(FLOW, ScrapeResult.FAILED, Duration.ofSeconds(1));

        // then
        assertEquals(1, itemCount(FLOW, "failed"));
    }

    @Test
    void shouldRecordDuration_inBucketsFromUnderASecondToManyMinutes() {
        // when
        adaptor.recordItem(FLOW, ScrapeResult.SUCCESS, Duration.ofMinutes(12));

        // then
        final var timer = registry.get("macro.scrape.item.duration").tag("flow", FLOW).timer();
        assertEquals(720, timer.totalTime(TimeUnit.SECONDS));
        final var bounds = Arrays.stream(timer.takeSnapshot().histogramCounts())
                .map(CountAtBucket::bucket)
                .toList();
        assertEquals(TimeUnit.MILLISECONDS.toNanos(500), bounds.getFirst());
        assertEquals(TimeUnit.MINUTES.toNanos(30), bounds.getLast());
    }

    @Test
    void shouldHaveNoLastSuccess_beforeTheFirstSuccess() {
        // when
        adaptor.recordItem(FLOW, ScrapeResult.FAILED, Duration.ofSeconds(1));

        // then
        assertNull(registry.find("macro.scrape.last.success.timestamp").tag("flow", FLOW).timeGauge());
    }

    @Test
    void shouldReportTimeOfLastSuccess_perFlow() {
        // given
        adaptor.recordItem(FLOW, ScrapeResult.SUCCESS, Duration.ofSeconds(1));
        final var succeededAt = now.get();
        now.set(START.plusSeconds(60));

        // when
        adaptor.recordItem(FLOW, ScrapeResult.FAILED, Duration.ofSeconds(1));

        // then
        assertEquals(succeededAt.getEpochSecond(), lastSuccess(FLOW).value(TimeUnit.SECONDS));
    }

    @Test
    void shouldReportLastSuccess_whenResultIsEmpty() {
        // when
        adaptor.recordItem(FLOW, ScrapeResult.EMPTY, Duration.ofSeconds(1));

        // then
        assertEquals(START.getEpochSecond(), lastSuccess(FLOW).value(TimeUnit.SECONDS));
    }

    @Test
    void shouldReadPendingItemsFromTheQueue() {
        // given
        when(scrapeQueuePort.countPending()).thenReturn(3);

        // when / then
        assertEquals(3, pending().value());
    }

    @Test
    void shouldReuseThePendingCount_withinTheReadInterval() {
        // given
        when(scrapeQueuePort.countPending()).thenReturn(3, 5);

        // when
        final var first = pending().value();
        now.set(START.plus(ScrapeMetricsMicrometerAdaptor.PENDING_READ_INTERVAL).minusSeconds(1));
        final var second = pending().value();

        // then
        assertEquals(3, first);
        assertEquals(3, second);
        verify(scrapeQueuePort, times(1)).countPending();
    }

    @Test
    void shouldReadPendingItemsAgain_afterTheReadInterval() {
        // given
        when(scrapeQueuePort.countPending()).thenReturn(3, 5);
        pending().value();

        // when
        now.set(START.plus(ScrapeMetricsMicrometerAdaptor.PENDING_READ_INTERVAL));

        // then
        assertEquals(5, pending().value());
    }

    @Test
    void shouldReportNaN_andNotRetryWithinTheInterval_whenThePendingReadFails() {
        // given
        when(scrapeQueuePort.countPending()).thenThrow(new RuntimeException("database down"));

        // when
        final var first = pending().value();
        final var second = pending().value();

        // then
        assertTrue(Double.isNaN(first));
        assertTrue(Double.isNaN(second));
        verify(scrapeQueuePort, times(1)).countPending();
    }

    private double itemCount(String flow, String result) {
        return registry.get("macro.scrape.items").tag("flow", flow).tag("result", result).counter().count();
    }

    private TimeGauge lastSuccess(String flow) {
        return registry.get("macro.scrape.last.success.timestamp").tag("flow", flow).timeGauge();
    }

    private Gauge pending() {
        return registry.get("macro.scrape.queue.pending").gauge();
    }

}
