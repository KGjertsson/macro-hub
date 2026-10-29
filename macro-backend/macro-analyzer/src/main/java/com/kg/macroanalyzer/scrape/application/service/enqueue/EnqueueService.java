package com.kg.macroanalyzer.scrape.application.service.enqueue;

import com.kg.macroanalyzer.scrape.application.port.in.EnqueueException;
import com.kg.macroanalyzer.scrape.application.port.out.ScrapeQueuePort;
import com.kg.macroanalyzer.scrape.application.service.enqueue.queuestrategy.QueueStrategyFactory;
import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem.ScrapeQueueItemBuilder;
import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem;
import com.kg.macroanalyzer.scrape.domain.ScrapeQueueKind;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.kg.macroanalyzer.scrape.application.service.enqueue.queuestrategy.QueueStrategyFactory.buildQueueStrategy;
import static java.util.Objects.isNull;

@Service
public class EnqueueService {

    private final List<SeriesConfig> availableSeries;
    private final ScrapeQueuePort scrapeQueuePort;

    @Autowired
    public EnqueueService(
            List<SeriesConfig> availableSeries,
            ScrapeQueuePort scrapeQueuePort
    ) {
        this.availableSeries = availableSeries;
        this.scrapeQueuePort = scrapeQueuePort;
    }

    public void enqueue(String name) throws EnqueueException {
        if (isNull(name)) {
            throw new EnqueueException("Found unexpected instance of null in enqueue");
        }

        availableSeries.stream()
                .map(SeriesConfig::name)
                .filter(n -> n.equals(name))
                .map(ScrapeQueueItem::of)
                .forEach(scrapeQueuePort::persistScrapeQueueItem);
    }

    public void enqueueAll() {
        final var queueTimeStrategy = buildQueueStrategy(QueueStrategyFactory.Strategy.INSTANT);
        final var builder = ScrapeQueueItem.builder();
        builder.scrapeQueueKind(ScrapeQueueKind.MACRO_POINT);
        final var currentQueue = scrapeQueuePort.getScrapeQueue().stream()
                .map(ScrapeQueueItem::name).toList();

        availableSeries.stream()
                .map(SeriesConfig::name)
                .filter(name -> !currentQueue.contains(name))
                .map(builder::name)
                .map(queueTimeStrategy::withTimeSlot)
                .map(ScrapeQueueItemBuilder::build)
                .forEach(scrapeQueuePort::persistScrapeQueueItem);
    }

}
