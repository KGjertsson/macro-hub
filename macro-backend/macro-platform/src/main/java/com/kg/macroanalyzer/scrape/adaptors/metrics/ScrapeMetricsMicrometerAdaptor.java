package com.kg.macroanalyzer.scrape.adaptors.metrics;

import com.kg.macroanalyzer.scrape.application.port.out.ScrapeMetricsPort;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.domain.ScrapeResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class ScrapeMetricsMicrometerAdaptor implements ScrapeMetricsPort {

    static final Duration PENDING_READ_INTERVAL = Duration.ofSeconds(30);

    private static final String ITEMS = "macro.scrape.items";
    private static final String ITEM_DURATION = "macro.scrape.item.duration";
    private static final String QUEUE_PENDING = "macro.scrape.queue.pending";
    private static final String LAST_SUCCESS = "macro.scrape.last.success.timestamp";

    private static final String FLOW_TAG = "flow";
    private static final String RESULT_TAG = "result";
    private static final String SUCCESS = "success";
    private static final String FAILED = "failed";

    // From an item already in the database, some 100 ms, to a first SCB scrape of about 12 minutes
    private static final Duration[] DURATION_BUCKETS = {
            Duration.ofMillis(500),
            Duration.ofSeconds(1),
            Duration.ofMillis(2500),
            Duration.ofSeconds(5),
            Duration.ofSeconds(10),
            Duration.ofSeconds(30),
            Duration.ofMinutes(1),
            Duration.ofMinutes(2),
            Duration.ofMinutes(5),
            Duration.ofMinutes(10),
            Duration.ofMinutes(20),
            Duration.ofMinutes(30)
    };

    private final MeterRegistry meterRegistry;
    private final ScrapeQueuePort scrapeQueuePort;
    private final InstantSource clock;
    private final Map<String, AtomicLong> lastSuccessMillis = new ConcurrentHashMap<>();
    private final AtomicReference<PendingReading> pendingReading =
            new AtomicReference<>(new PendingReading(Instant.MIN, Double.NaN));

    @Autowired
    public ScrapeMetricsMicrometerAdaptor(MeterRegistry meterRegistry, ScrapeQueuePort scrapeQueuePort) {
        this(meterRegistry, scrapeQueuePort, InstantSource.system());
    }

    ScrapeMetricsMicrometerAdaptor(MeterRegistry meterRegistry, ScrapeQueuePort scrapeQueuePort, InstantSource clock) {
        this.meterRegistry = meterRegistry;
        this.scrapeQueuePort = scrapeQueuePort;
        this.clock = clock;
        Gauge.builder(QUEUE_PENDING, this, ScrapeMetricsMicrometerAdaptor::pendingItems)
                .description("Queue items not done yet, read from the database at most every 30 seconds")
                .register(meterRegistry);
    }

    // A counter that first appears with the value 1 shows no increase in Prometheus, so every series starts at 0
    @Override
    public void registerFlows(Collection<String> flowIds) {
        flowIds.forEach(flowId -> {
            itemCounter(flowId, SUCCESS);
            itemCounter(flowId, FAILED);
            durationTimer(flowId);
        });
        itemCounter(UNKNOWN_FLOW, FAILED);
    }

    @Override
    public void recordItem(String flowId, ScrapeResult result, Duration duration) {
        final var failed = ScrapeResult.isFailed(result);
        itemCounter(flowId, failed ? FAILED : SUCCESS).increment();
        durationTimer(flowId).record(duration);
        if (!failed) {
            lastSuccessGauge(flowId).set(clock.millis());
        }
    }

    private Counter itemCounter(String flowId, String result) {
        return Counter.builder(ITEMS)
                .description("Queue items processed")
                .tag(FLOW_TAG, flowId)
                .tag(RESULT_TAG, result)
                .register(meterRegistry);
    }

    private Timer durationTimer(String flowId) {
        return Timer.builder(ITEM_DURATION)
                .description("Time to process one queue item")
                .tag(FLOW_TAG, flowId)
                .serviceLevelObjectives(DURATION_BUCKETS)
                .register(meterRegistry);
    }

    // In memory, because the queue table doesn't say when an item was done, so a flow has no series until its first
    // success after a start. The Scrapes dashboard bridges that with last_over_time
    private AtomicLong lastSuccessGauge(String flowId) {
        return lastSuccessMillis.computeIfAbsent(flowId, id -> {
            final var millis = new AtomicLong();
            TimeGauge.builder(LAST_SUCCESS, millis, TimeUnit.MILLISECONDS, AtomicLong::doubleValue)
                    .description("Time of the last item that wasn't failed")
                    .tag(FLOW_TAG, id)
                    .register(meterRegistry);
            return millis;
        });
    }

    // A failed read is remembered too: a Prometheus scrape must not wait out the pool's connection timeout every time
    private double pendingItems() {
        final var now = clock.instant();
        final var cached = pendingReading.get();
        if (cached.readAt().plus(PENDING_READ_INTERVAL).isAfter(now)) {
            return cached.pending();
        }

        final var refreshed = new PendingReading(now, readPendingItems());
        pendingReading.set(refreshed);

        return refreshed.pending();
    }

    private double readPendingItems() {
        try {
            return scrapeQueuePort.countPending();
        } catch (RuntimeException e) {
            log.warn("Could not read the number of pending queue items", e);

            return Double.NaN;
        }
    }

    private record PendingReading(Instant readAt, double pending) {
    }

}
