package com.kg.macroanalyzer.application.services.bundleformat.timefilterstrategy;

import com.kg.macroanalyzer.application.domain.MacroSeries;

import java.util.List;

public interface TimeFilterStrategy {

    List<MacroSeries> filter(List<MacroSeries> macroSeriesList);

}
