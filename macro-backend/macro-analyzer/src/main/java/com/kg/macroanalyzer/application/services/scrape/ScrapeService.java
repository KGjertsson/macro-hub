package com.kg.macroanalyzer.application.services.scrape;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMacroPoints;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.ScrapeFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class ScrapeService {

    private final DatabasePort databasePort;
    private final ScrapeFlow<ScrapeQueueItem, SeriesConfig, ConfigWithMacroPoints> macroPointScrapeFlow;

    @Autowired
    public ScrapeService(
            DatabasePort databasePort,
            ScrapeFlow<ScrapeQueueItem, SeriesConfig, ConfigWithMacroPoints> macroPointScrapeFlow
    ) {
        this.databasePort = databasePort;
        this.macroPointScrapeFlow = macroPointScrapeFlow;
    }

    public List<ScrapeResult> scrapeFromQueue(LocalDateTime timeStamp) {
        return fetchItemsToScrape(timeStamp)
                .map(macroPointScrapeFlow::run)
                .toList();
    }

    private Stream<ScrapeQueueItem> fetchItemsToScrape(LocalDateTime timeStamp) {
        final var items = databasePort.getItemsToScrape(timeStamp);
        final var msg = "Scraping items from queue: %s".formatted(items);
        log.info(msg);

        return items.stream();
    }

}
