package com.kg.macroanalyzer.application.services.bundleformat.timefilterstrategy;

import com.kg.macroanalyzer.application.domain.MacroSeries;

import java.util.List;

public class TimeFilterStrategyAll implements TimeFilterStrategy {

    @Override
    public List<MacroSeries> filter(List<MacroSeries> macroSeriesList) {
        return macroSeriesList;
    }

}
