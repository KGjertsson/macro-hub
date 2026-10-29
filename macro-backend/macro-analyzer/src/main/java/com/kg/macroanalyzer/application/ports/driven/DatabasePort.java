package com.kg.macroanalyzer.application.ports.driven;

import com.kg.macroanalyzer.application.domain.macroseries.MacroSeries;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;

import java.util.List;

public interface DatabasePort {

    List<MacroSeries> readMacroSeries(List<SeriesConfig> paramList);

    MacroSeries readMacroSeries(SeriesConfig paramList);

    Integer writeMacroPoints(ConfigWithMacroPoints configWithMacroPoints);

}
