package com.kg.macroanalyzer.application.services;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public void enqueue(String name) {
        availableSeries.stream()
                .map(SeriesConfig::name)
                .filter(n -> n.equals(name))
                .map(ScrapeQueueItem::of)
                .forEach(databasePort::persist);
    }

    public void enqueueAll() {
        // TODO: don't forget spacing in between
    }

}
