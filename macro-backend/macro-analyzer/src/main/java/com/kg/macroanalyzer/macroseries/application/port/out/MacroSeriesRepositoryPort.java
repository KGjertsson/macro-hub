package com.kg.macroanalyzer.macroseries.application.port.out;

import com.kg.macroanalyzer.macroseries.domain.MacroSeries;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;

import java.util.List;

public interface MacroSeriesRepositoryPort {

    List<MacroSeries> readMacroSeries(List<SeriesConfig> paramList);

    MacroSeries readMacroSeries(SeriesConfig paramList);

    Integer writeMacroPoints(ConfigWithMacroPoints configWithMacroPoints);

}
