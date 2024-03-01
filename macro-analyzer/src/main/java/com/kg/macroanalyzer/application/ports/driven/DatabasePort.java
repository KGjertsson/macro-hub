package com.kg.macroanalyzer.application.ports.driven;

import com.kg.macroanalyzer.adaptors.database.postgres.models.ScrapeQueueItem;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.ports.driving.out.chartdata.ChartSeriesParam;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;

import java.util.List;

public interface DatabasePort {

    List<MacroSeries> readMacroSeries(List<ChartSeriesParam> paramList);

    List<SeriesConfig> readSeriesConfigList();

    void persist(ScrapeQueueItem scrapeQueueItem);

}
