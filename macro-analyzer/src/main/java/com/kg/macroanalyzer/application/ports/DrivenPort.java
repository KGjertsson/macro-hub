package com.kg.macroanalyzer.application.ports;

import com.kg.macroanalyzer.application.domain.MacroBundle;

import java.util.List;

public interface DrivenPort {

    MacroBundle readMacroBundle(List<String> names);

}
