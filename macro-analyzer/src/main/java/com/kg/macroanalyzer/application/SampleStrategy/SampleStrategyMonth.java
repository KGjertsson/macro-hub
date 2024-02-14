package com.kg.macroanalyzer.application.SampleStrategy;

import com.kg.macroanalyzer.application.domain.MacroBundle;

import java.util.Optional;

public class SampleStrategyMonth implements SampleStrategy {

    @Override
    public Optional<MacroBundle> sample(MacroBundle macroBundle) {
        return Optional.empty();
    }

}
