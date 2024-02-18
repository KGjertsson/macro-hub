package com.kg.macroanalyzer.application.ports.driving;

import com.kg.macroanalyzer.application.BundleFormatter;
import com.kg.macroanalyzer.application.ports.DatabasePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RestApiPort implements DrivingPort {

    private final DatabasePort databasePort;
    private final BundleFormatter bundleFormatter;

    @Autowired
    public RestApiPort(DatabasePort databasePort, BundleFormatter bundleFormatter) {
        this.databasePort = databasePort;
        this.bundleFormatter = bundleFormatter;
    }

    @Override
    public ChartDataWithLabels buildChartData(BuildChartDataParams params) {
        final var blueBerry = databasePort.readMacroBundle(params);
        final var raspBerry = blueBerry.map(bundle -> bundleFormatter.align(bundle, params.strategy()));

        return null;
    }

}
