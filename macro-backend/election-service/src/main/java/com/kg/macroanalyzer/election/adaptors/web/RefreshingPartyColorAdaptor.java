package com.kg.macroanalyzer.election.adaptors.web;

import com.kg.macroanalyzer.election.application.port.out.PartyColorPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Keeps the last party list in memory and refreshes it on its own thread (ADR 0005). Not on @Scheduled, which
// scheduler.enabled turns off along with the scrapes: without a refresh, election would stay on Valmyndigheten's colours
@Slf4j
public class RefreshingPartyColorAdaptor implements PartyColorPort, AutoCloseable {

    private final ParliamentPartiesClient client;
    private final Duration refreshInterval;
    private final Duration retryInterval;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
            Thread.ofPlatform().name("party-color-refresh").daemon().factory()
    );

    private volatile Map<String, String> colors = Map.of();

    public RefreshingPartyColorAdaptor(ParliamentPartiesClient client, Duration refreshInterval, Duration retryInterval) {
        if (!refreshInterval.isPositive() || !retryInterval.isPositive()) {
            throw new IllegalArgumentException("The refresh and retry intervals must be positive");
        }
        this.client = client;
        this.refreshInterval = refreshInterval;
        this.retryInterval = retryInterval;
    }

    @Override
    public Map<String, String> partyColors() {
        return colors;
    }

    // On its own thread, so start-up never waits for parliament, which may not be up yet
    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        schedule(Duration.ZERO);
    }

    private void schedule(Duration delay) {
        executor.schedule(this::refreshAndScheduleNext, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void refreshAndScheduleNext() {
        try {
            refresh();
        } finally {
            // Also after an Error, which would otherwise end the refreshing for good, unnoticed
            schedule(colors.isEmpty() ? retryInterval : refreshInterval);
        }
    }

    private void refresh() {
        try {
            final var fetched = client.fetchPartyColors();
            if (fetched.isEmpty()) {
                log.warn("Parliament has no party list, keeping the {} party colours held", colors.size());
            } else {
                colors = fetched;
                log.info("Loaded {} party colours", fetched.size());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Could not refresh the party colours, keeping the {} held: {}", colors.size(), e.toString());
        }
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }

}
