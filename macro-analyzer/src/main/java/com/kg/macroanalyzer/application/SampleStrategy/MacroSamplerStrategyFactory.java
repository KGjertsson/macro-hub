package com.kg.macroanalyzer.application.SampleStrategy;

import org.springframework.stereotype.Component;

@Component
public class MacroSamplerStrategyFactory {

    public SampleStrategy build() {
        return new SampleStrategyMonth();
    }

}
