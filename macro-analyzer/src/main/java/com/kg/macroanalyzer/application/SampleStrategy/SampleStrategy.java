package com.kg.macroanalyzer.application.SampleStrategy;

import com.kg.macroanalyzer.application.domain.MacroBundle;

import java.util.Optional;

public interface SampleStrategy {

    Optional<MacroBundle> sample(MacroBundle macroBundle);

}
