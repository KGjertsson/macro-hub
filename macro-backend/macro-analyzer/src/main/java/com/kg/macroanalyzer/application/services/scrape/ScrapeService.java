package com.kg.macroanalyzer.application.services.scrape;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueKind;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMacroPoints;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMemberOfParliament;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.application.services.scrape.scrapeflow.ScrapeFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class ScrapeService {

    private final DatabasePort databasePort;
    private final ScrapeFlow<ScrapeQueueItem, SeriesConfig, ConfigWithMacroPoints> macroPointScrapeFlow;
    private final ScrapeFlow<ScrapeQueueItem, SeriesConfig, ConfigWithMemberOfParliament> memberScrapeFlow;

    @Autowired
    public ScrapeService(
            DatabasePort databasePort,
            @Qualifier("macroPointScrapeFlow") ScrapeFlow<ScrapeQueueItem, SeriesConfig, ConfigWithMacroPoints> macroPointScrapeFlow,
            @Qualifier("memberScrapeFlow") ScrapeFlow<ScrapeQueueItem, SeriesConfig, ConfigWithMemberOfParliament> memberScrapeFlow
    ) {
        this.databasePort = databasePort;
        this.macroPointScrapeFlow = macroPointScrapeFlow;
        this.memberScrapeFlow = memberScrapeFlow;
    }

    public List<ScrapeResult> scrapeFromQueue(LocalDateTime timeStamp) {
        return fetchItemsToScrape(timeStamp)
                .map(this::scrape)
                .toList();
    }

    private Stream<ScrapeQueueItem> fetchItemsToScrape(LocalDateTime timeStamp) {
        final var items = databasePort.getItemsToScrape(timeStamp);
        final var msg = "Scraping items from queue: %s".formatted(items);
        log.info(msg);

        return items.stream();
    }

    private ScrapeResult scrape(ScrapeQueueItem scrapeQueueItem) {
        final var scrapeFlow = selectScrapeFlow(scrapeQueueItem.scrapeQueueKind());

        return scrapeFlow.run(scrapeQueueItem);
    }

    private ScrapeFlow<ScrapeQueueItem, ?, ?> selectScrapeFlow(ScrapeQueueKind scrapeQueueKind) {
        switch (scrapeQueueKind) {
            case MACRO_POINT -> {
                return macroPointScrapeFlow;
            }
            case MEMBER_OF_PARLIAMENT -> {
                return memberScrapeFlow;
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported scrape queue kind: %s".formatted(scrapeQueueKind)
            );
        }
    }

}
