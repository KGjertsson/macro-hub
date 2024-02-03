package com.kg.macroanalyzer.application;

import com.kg.macroanalyzer.application.domain.MacroBundle;
import org.springframework.stereotype.Component;

@Component
public class MacroSampler {

    public MacroBundle sample(Formatter.WithFullLabels bundleWithFullLabels) {
        return bundleWithFullLabels.macroBundle();
    }

}
