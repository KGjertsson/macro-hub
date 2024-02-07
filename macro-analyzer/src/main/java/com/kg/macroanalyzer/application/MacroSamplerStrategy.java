package com.kg.macroanalyzer.application;

import com.kg.macroanalyzer.application.domain.MacroBundle;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MacroSamplerStrategy {

    public Optional<MacroBundle> sample(MacroBundle macroBundle) {


        return Optional.of(macroBundle);
    }

}
