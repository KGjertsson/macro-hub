package com.kg.macroanalyzer.application.ports.driven;

import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record ConfigWithMacroPoints(SeriesConfig seriesConfig, List<MacroPoint> macroPoints) {
}
