package com.kg.macroanalyzer.scrape.application.service.scrapeflow;

import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;

import com.kg.macroanalyzer.shared.collection.ImmutableCopies;

import java.util.List;
import java.util.Optional;

public record SeriesConfigPreparer(
        List<SeriesConfig> seriesConfigList
) implements Preparer<ScrapeQueueItem, SeriesConfig> {

    public SeriesConfigPreparer {
        seriesConfigList = ImmutableCopies.list(seriesConfigList);
    }

    @Override
    public Optional<SeriesConfig> prepare(ScrapeQueueItem scrapeQueueItem) {
        return seriesConfigList.stream()
                .filter(c -> c.name().equals(scrapeQueueItem.name()))
                .findFirst();
    }

}
