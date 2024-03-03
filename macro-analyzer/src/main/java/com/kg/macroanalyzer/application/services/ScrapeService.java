package com.kg.macroanalyzer.application.services;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.application.ports.driven.ConfigWithMacroPoints;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.utils.ScrapeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
public class ScrapeService {

    private final DatabasePort databasePort;
    private final List<SeriesConfig> seriesConfigList;

    @Autowired
    public ScrapeService(
            DatabasePort databasePort,
            List<SeriesConfig> seriesConfigList
    ) {
        this.databasePort = databasePort;
        this.seriesConfigList = seriesConfigList;
    }

    public void scrapeFromQueue(LocalDateTime timeStamp) {
        fetchItemsToScrape(timeStamp)
                .forEach(this::scrape);
    }

    private Stream<ScrapeQueueItem> fetchItemsToScrape(LocalDateTime timeStamp) {
        final var items = databasePort.getItemsToScrape(timeStamp);
        log.info("Scraping items from queue: %s".formatted(items));

        return items.stream();
    }

    private void scrape(ScrapeQueueItem scrapeQueueItem) {
        toSeriesConfig(scrapeQueueItem)
                .flatMap(this::findExisting)
                .map(this::scrape)
                .ifPresent(this::persist);
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

    private ConfigWithMacroPoints scrape(ConfigWithMacroPoints configWithMacroPoints) {
        try {
            final var config = configWithMacroPoints.seriesConfig();
            final var macroPoints = configWithMacroPoints.macroPoints();
            final var novelScraped = ScrapeUtils.scrapeNovelItems(config, macroPoints);

            return configWithMacroPoints.toBuilder()
                    .macroPoints(novelScraped)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void persist(ConfigWithMacroPoints configWithMacroPoints) {
        final var persistedCount = databasePort.writeMacroPoints(configWithMacroPoints);
        final var name = configWithMacroPoints.seriesConfig().name();
        final var config = configWithMacroPoints.seriesConfig();
        log.info("Persist %s novel macro points for %s".formatted(persistedCount, name));

        databasePort.markAsDone(config);
    }

}
