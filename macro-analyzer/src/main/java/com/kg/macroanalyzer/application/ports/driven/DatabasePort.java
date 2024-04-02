package com.kg.macroanalyzer.application.ports.driven;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;

import java.time.LocalDateTime;
import java.util.List;

public interface DatabasePort {

    List<MacroSeries> readMacroSeries(List<SeriesConfig> paramList);

    MacroSeries readMacroSeries(SeriesConfig paramList);

    List<SeriesConfig> readSeriesConfigList();

    void persistScrapeQueueItem(ScrapeQueueItem scrapeQueueItem);

    List<ScrapeQueueItem> getScrapeQueue();

    List<ScrapeQueueItem> getItemsToScrape(LocalDateTime timeStamp);

    Integer writeMacroPoints(ConfigWithMacroPoints configWithMacroPoints);

    void markAsDone(SeriesConfig seriesConfig);

}
