package com.kg.macroanalyzer.macroseries.adaptors.http;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ColorSelectionStrategy {

    private final static List<String> PRE_DEFINED_COLORS = List.of(
            "rgb(207,82,48)",
            "rgb(245,154,68)",
            "rgb(227,197,152)",
            "rgb(110,97,47)",
            "rgb(110,53,44)"
    );

    // Cycles through the palette, so any number of series gets a colour (index is zero-based)
    public String pickColor(Integer index) {
        return PRE_DEFINED_COLORS.get(Math.floorMod(index, PRE_DEFINED_COLORS.size()));
    }

}
