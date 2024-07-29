package com.kg.macroanalyzer.application.services.enqueue;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem.ScrapeQueueItemBuilder;
import com.kg.macroanalyzer.application.exceptions.EnqueueException;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.application.services.enqueue.queuestrategy.QueueStrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.kg.macroanalyzer.application.services.enqueue.queuestrategy.QueueStrategyFactory.buildQueueStrategy;
import static java.util.Objects.isNull;

@Service
public class EnqueueService {

    private final List<SeriesConfig> availableSeries;
    private final DatabasePort databasePort;

    @Autowired
    public EnqueueService(
            List<SeriesConfig> availableSeries,
            DatabasePort databasePort
    ) {
        this.availableSeries = availableSeries;
        this.databasePort = databasePort;
    }

    public void enqueue(String name) throws EnqueueException {
        if (isNull(name)) {
            throw new EnqueueException("Found unexpected instance of null in enqueue");
        }

        availableSeries.stream()
                .map(SeriesConfig::name)
                .filter(n -> n.equals(name))
                .map(ScrapeQueueItem::of)
                .forEach(databasePort::persistScrapeQueueItem);
    }

    public void enqueueAll() {
        final var queueTimeStrategy = buildQueueStrategy(QueueStrategyFactory.Strategy.INSTANT);
        final var builder = ScrapeQueueItem.builder();
        final var currentQueue = databasePort.getScrapeQueue().stream()
                .map(ScrapeQueueItem::name).toList();

        availableSeries.stream()
                .map(SeriesConfig::name)
                .filter(name -> !currentQueue.contains(name))
                .map(builder::name)
                .map(queueTimeStrategy::withTimeSlot)
                .map(ScrapeQueueItemBuilder::build)
                .forEach(databasePort::persistScrapeQueueItem);
    }

}
