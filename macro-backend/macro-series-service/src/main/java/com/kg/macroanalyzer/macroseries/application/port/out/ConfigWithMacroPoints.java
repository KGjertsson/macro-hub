package com.kg.macroanalyzer.macroseries.application.port.out;

import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record ConfigWithMacroPoints(SeriesConfig seriesConfig, List<MacroPoint> macroPoints) {
}
