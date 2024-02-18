package com.kg.macroanalyzer.application.ports;

import com.kg.macroanalyzer.application.domain.MacroBundle;
import com.kg.macroanalyzer.application.ports.driving.BuildChartDataParams;

import java.util.Optional;

public interface DatabasePort {

    Optional<MacroBundle> readMacroBundle(BuildChartDataParams params);

}
