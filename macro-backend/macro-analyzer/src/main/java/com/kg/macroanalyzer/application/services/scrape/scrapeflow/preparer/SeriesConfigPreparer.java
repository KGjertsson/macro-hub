package com.kg.macroanalyzer.application.services.scrape.scrapeflow.preparer;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;

import java.util.List;
import java.util.Optional;

public record SeriesConfigPreparer(
        List<SeriesConfig> seriesConfigList
) implements Preparer<ScrapeQueueItem, SeriesConfig> {

    @Override
    public Optional<SeriesConfig> prepare(ScrapeQueueItem scrapeQueueItem) {
        return seriesConfigList.stream()
                .filter(c -> c.name().equals(scrapeQueueItem.name()))
                .findFirst();
    }

}
