package com.kg.macroanalyzer.application.domain;


import lombok.Builder;
import lombok.NonNull;

import java.util.List;

@Builder
public record MacroBundle(@NonNull List<MacroSeries> macroSeries) {
}
