package com.kg.macroanalyzer.adaptors.database.postgres;

import com.kg.macroanalyzer.application.domain.MacroBundle;
import com.kg.macroanalyzer.application.ports.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.BuildChartDataParams;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PostgresAdaptor implements DatabasePort {

    @Override
    public Optional<MacroBundle> readMacroBundle(BuildChartDataParams params) {
        return Optional.empty();
    }

}
