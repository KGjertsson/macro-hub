package com.kg.macroanalyzer.application.services.bundleformat.samplestrategy;

import com.kg.macroanalyzer.application.domain.AlignedBundle;
import com.kg.macroanalyzer.application.domain.MacroSeries;

import java.util.List;
import java.util.Optional;

public interface SampleStrategy {

    Optional<AlignedBundle> sample(List<MacroSeries> macroSeriesList);

}
