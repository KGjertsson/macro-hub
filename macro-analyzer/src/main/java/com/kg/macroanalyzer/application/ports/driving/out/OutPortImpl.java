package com.kg.macroanalyzer.application.ports.driving.out;

import com.kg.macroanalyzer.application.domain.AlignedBundle;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.chartdata.BuildChartDataParams;
import com.kg.macroanalyzer.application.ports.driving.out.chartdata.ChartSeriesParam;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.application.services.bundleformat.BundleFormatService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OutPortImpl implements OutPort {

    private final DatabasePort databasePort;
    private final BundleFormatService bundleFormatService;
    private final List<SeriesConfig> seriesConfigList;

    @Autowired
    public OutPortImpl(
            DatabasePort databasePort,
            BundleFormatService bundleFormatService
    ) {
        this.databasePort = databasePort;
        this.bundleFormatService = bundleFormatService;
        this.seriesConfigList = databasePort.readSeriesConfigList();
    }

    @Override
    public Optional<AlignedBundle> buildAlignedBundle(@NonNull BuildChartDataParams params) {
        final var strategy = params.strategy();

        return Optional.of(params)
                .map(BuildChartDataParams::chartSeriesParams)
                .map(this::readMacroSeries)
                .flatMap(macroSeriesList -> bundleFormatService.align(macroSeriesList, strategy));
    }

    @Override
    public List<SeriesConfig> getSeriesConfigList() {
        return seriesConfigList;
    }

    private List<MacroSeries> readMacroSeries(
            @NonNull List<ChartSeriesParam> chartSeriesParamList
    ) {
        final var chartSeriesParamNames = chartSeriesParamList.stream()
                .map(ChartSeriesParam::name)
                .toList();

        final var requestedMacroSeries = seriesConfigList.stream()
                .filter(c -> chartSeriesParamNames.contains(c.name()))
                .toList();

        return databasePort.readMacroSeries(requestedMacroSeries);
    }

}
