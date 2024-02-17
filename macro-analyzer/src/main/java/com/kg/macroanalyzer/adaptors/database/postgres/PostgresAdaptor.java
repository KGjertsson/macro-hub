package com.kg.macroanalyzer.adaptors.database.postgres;

import com.kg.macroanalyzer.application.ports.DrivenPort;
import com.kg.macroanalyzer.application.domain.MacroBundle;

import java.util.List;

public class PostgresAdaptor implements DrivenPort {

    @Override
    public MacroBundle readMacroBundle(List<String> names) {
        return null;
    }

}
