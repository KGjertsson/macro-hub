package com.kg.macroanalyzer.application.domain;


import lombok.Builder;

import java.util.List;

@Builder
public record MacroBundle(List<MacroSeries> macroSeries) {
}
