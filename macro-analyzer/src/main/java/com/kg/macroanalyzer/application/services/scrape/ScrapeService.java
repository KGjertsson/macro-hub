package com.kg.macroanalyzer.application.services.scrape;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMacroPoints;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
public class ScrapeService {

    private final DatabasePort databasePort;
    private final ScrapeAdaptor scrapeAdaptor;
    private final List<SeriesConfig> seriesConfigList;

    @Autowired
    public ScrapeService(
            DatabasePort databasePort,
            ScrapeAdaptor scrapeAdaptor,
            List<SeriesConfig> seriesConfigList
    ) {
        this.databasePort = databasePort;
        this.scrapeAdaptor = scrapeAdaptor;
        this.seriesConfigList = seriesConfigList;
    }

    public List<ScrapeResult> scrapeFromQueue(LocalDateTime timeStamp) {
        return fetchItemsToScrape(timeStamp)
                .map(this::scrape)
                .toList();
    }

    private Stream<ScrapeQueueItem> fetchItemsToScrape(LocalDateTime timeStamp) {
        final var items = databasePort.getItemsToScrape(timeStamp);
        log.info("Scraping items from queue: %s".formatted(items));

        return items.stream();
    }

    private ScrapeResult scrape(ScrapeQueueItem scrapeQueueItem) {
        return toSeriesConfig(scrapeQueueItem)
                .flatMap(this::findExisting)
                .flatMap(this::scrape)
                .map(this::persist)
                .orElse(ScrapeResult.FAILED);
    }

    private Optional<SeriesConfig> toSeriesConfig(ScrapeQueueItem scrapeQueueItem) {
        return seriesConfigList.stream()
                .filter(c -> c.name().equals(scrapeQueueItem.name()))
                .findFirst();
    }

    private Optional<ConfigWithMacroPoints> findExisting(SeriesConfig seriesConfig) {
        final var existing = databasePort.readMacroSeries(seriesConfig)
                .macroPoints();

        return Optional.of(new ConfigWithMacroPoints(seriesConfig, existing));
    }

    private Optional<ConfigWithMacroPoints> scrape(ConfigWithMacroPoints configWithMacroPoints) {
        try {
            final var config = configWithMacroPoints.seriesConfig();
            final var macroPoints = configWithMacroPoints.macroPoints();
            final var novelScraped = scrapeAdaptor.scrapeNovelItems(config, macroPoints);

            return Optional.ofNullable(
                    configWithMacroPoints.toBuilder()
                            .macroPoints(novelScraped)
                            .build()
            );
        } catch (ScrapeException scrapeException) {
            return Optional.empty();
        }
    }

    private ScrapeResult persist(ConfigWithMacroPoints configWithMacroPoints) {
        final var name = configWithMacroPoints.seriesConfig().name();
        if (configWithMacroPoints.macroPoints().isEmpty()) {
            log.info("Found no new macro points for %s".formatted(name));

            return ScrapeResult.EMPTY;
        }
        final var persistedCount = databasePort.writeMacroPoints(configWithMacroPoints);
        final var config = configWithMacroPoints.seriesConfig();
        log.info("Persist %s novel macro points for %s".formatted(persistedCount, name));
        databasePort.markAsDone(config);

        return ScrapeResult.SUCCESS;
    }

}
