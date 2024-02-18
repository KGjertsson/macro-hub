package com.kg.macroanalyzer.application.ports.driving;

import com.kg.macroanalyzer.application.BundleFormatter;
import com.kg.macroanalyzer.application.domain.AlignedBundle;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public Optional<ChartDataWithLabels> buildChartData(BuildChartDataParams params) {
        final var chartSeriesParamList = params.chartSeriesParams();
        final var strategy = params.strategy();

        final var macroSeriesList = databasePort.readMacroSeries(chartSeriesParamList);
        return bundleFormatter.align(macroSeriesList, strategy)
                .map(this::buildResponse);
    }

    private ChartDataWithLabels buildResponse(AlignedBundle alignedBundle) {
        final var labels = alignedBundle.labels();
        final var values = alignedBundle.macroSeries().stream()
                .map(this::buildChartData).toList();

        return ChartDataWithLabels.builder()
                .chartData(values)
                .labels(labels)
                .build();
    }

    private ChartData buildChartData(MacroSeries macroSeries) {
        return ChartData.builder()
                .name(macroSeries.name())
                .values(macroSeries.getValues())
                .color("rgb(207,82,48)")
                .build();
    }

}
