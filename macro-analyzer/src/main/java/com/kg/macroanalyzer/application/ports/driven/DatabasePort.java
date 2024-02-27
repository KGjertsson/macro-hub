package com.kg.macroanalyzer.application.ports.driven;

import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.ports.driving.chartdata.ChartSeriesParam;
import com.kg.macroanalyzer.application.ports.driving.seriesconfig.SeriesConfig;

import java.util.List;

public interface DatabasePort {

    List<MacroSeries> readMacroSeries(List<ChartSeriesParam> paramList);

    List<SeriesConfig> readSeriesConfigList();

}
