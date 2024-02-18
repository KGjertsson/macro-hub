package com.kg.macroanalyzer.application.ports.driving;

import com.kg.macroanalyzer.application.BundleFormatter;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
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
        final var chartSeriesParamList = params.chartSeriesParams();

        final var blueBerry = databasePort.readMacroSeries(chartSeriesParamList);
        final var raspBerry = bundleFormatter.align(blueBerry, params.strategy());
        final var strawBerry = raspBerry.map(b -> {
            final var labels = b.labels();
            final var values = b.macroSeries().stream()
                    .map(series ->
                            ChartData.builder()
                                    .values(series.macroPoints().stream().map(MacroPoint::value).toList())
                                    .name(series.name())
                                    .build()
                    ).toList();

            return ChartDataWithLabels.builder()
                    .chartData(values)
                    .labels(labels)
                    .build();
        });

        return strawBerry.orElse(null);
    }

}
