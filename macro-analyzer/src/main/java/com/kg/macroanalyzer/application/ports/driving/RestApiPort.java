package com.kg.macroanalyzer.application.ports.driving;

import com.kg.macroanalyzer.application.BundleFormatter;
import com.kg.macroanalyzer.application.domain.AlignedBundle;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.chartdata.BuildChartDataParams;
import com.kg.macroanalyzer.application.ports.driving.seriesconfig.SeriesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public Optional<AlignedBundle> buildAlignedBundle(BuildChartDataParams params) {
        final var chartSeriesParamList = params.chartSeriesParams();
        final var strategy = params.strategy();
        final var macroSeriesList = databasePort.readMacroSeries(chartSeriesParamList);

        return bundleFormatter.align(macroSeriesList, strategy);
    }

    @Override
    public List<SeriesConfig> getSeriesConfigList() {
        return databasePort.readSeriesConfigList();
    }

}
