package com.kg.macroanalyzer.adaptors.database;

import com.kg.macroanalyzer.application.domain.MacroBundle;

import java.util.List;

public interface DatabaseAdaptor {

    MacroBundle readMacroBundle(List<String> names);

}
