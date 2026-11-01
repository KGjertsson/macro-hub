package com.kg.macroanalyzer.macroseries.application.service.bundleformat.timefilterstrategy;

import com.kg.macroanalyzer.macroseries.domain.MacroSeries;

import java.util.List;

public interface TimeFilterStrategy {

    List<MacroSeries> filter(List<MacroSeries> macroSeriesList);

}
