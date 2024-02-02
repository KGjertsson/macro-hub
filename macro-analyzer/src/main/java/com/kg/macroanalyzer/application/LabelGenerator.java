package com.kg.macroanalyzer.application;

import com.kg.macroanalyzer.application.domain.MacroBundle;

import java.util.List;

public class LabelGenerator {

    public Formatter.WithFullLabels generateDays(MacroBundle macroBundle) {
        return new Formatter.WithFullLabels(macroBundle, List.of());
    }

}
