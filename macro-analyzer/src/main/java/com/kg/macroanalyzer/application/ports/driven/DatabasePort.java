package com.kg.macroanalyzer.application.ports.driven;

import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.ports.driving.ChartSeriesParam;

import java.util.List;

public interface DatabasePort {

    List<MacroSeries> readMacroSeries(List<ChartSeriesParam> paramList);

}
