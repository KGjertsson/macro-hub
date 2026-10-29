package com.kg.macroanalyzer.macroseries.application.port.in;

import com.kg.macroanalyzer.macroseries.domain.AlignedBundle;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;

import java.util.List;
import java.util.Optional;

public interface MacroSeriesQueries {

    Optional<AlignedBundle> buildAlignedBundle(BuildChartDataParams params);

    List<SeriesConfig> getSeriesConfigList();

    List<SeriesConfig> getChartSeriesConfigList();

}
