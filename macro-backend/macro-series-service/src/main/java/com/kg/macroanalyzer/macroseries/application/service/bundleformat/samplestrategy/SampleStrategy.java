package com.kg.macroanalyzer.macroseries.application.service.bundleformat.samplestrategy;

import com.kg.macroanalyzer.macroseries.domain.AlignedBundle;
import com.kg.macroanalyzer.macroseries.domain.MacroSeries;

import java.util.List;
import java.util.Optional;

public interface SampleStrategy {

    Optional<AlignedBundle> sample(List<MacroSeries> macroSeriesList);

}
