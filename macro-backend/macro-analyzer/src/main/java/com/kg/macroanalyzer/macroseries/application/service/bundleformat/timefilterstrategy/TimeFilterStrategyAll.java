package com.kg.macroanalyzer.macroseries.application.service.bundleformat.timefilterstrategy;

import com.kg.macroanalyzer.macroseries.domain.MacroSeries;

import java.util.List;

public class TimeFilterStrategyAll implements TimeFilterStrategy {

    @Override
    public List<MacroSeries> filter(List<MacroSeries> macroSeriesList) {
        return macroSeriesList;
    }

}
