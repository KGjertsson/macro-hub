package com.kg.macroanalyzer.macroseries.application.port.out;

import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import com.kg.macroanalyzer.shared.collection.ImmutableCopies;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record ConfigWithMacroPoints(SeriesConfig seriesConfig, List<MacroPoint> macroPoints) {

    public ConfigWithMacroPoints {
        macroPoints = ImmutableCopies.list(macroPoints);
    }

}
