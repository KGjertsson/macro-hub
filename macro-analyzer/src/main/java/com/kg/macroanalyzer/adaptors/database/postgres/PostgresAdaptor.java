package com.kg.macroanalyzer.adaptors.database.postgres;

import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.ChartSeriesParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostgresAdaptor implements DatabasePort {

    @Override
    public List<MacroSeries> readMacroSeries(List<ChartSeriesParam> paramList) {
        return List.of();
    }

}
