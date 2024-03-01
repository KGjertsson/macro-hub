package com.kg.macroanalyzer.application.ports.driving.out;

import com.kg.macroanalyzer.application.services.bundleformatservice.BundleFormatService;
import com.kg.macroanalyzer.application.domain.AlignedBundle;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.chartdata.BuildChartDataParams;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OutPortImpl implements OutPort {

    private final DatabasePort databasePort;
    private final BundleFormatService bundleFormatService;

    @Autowired
    public OutPortImpl(DatabasePort databasePort, BundleFormatService bundleFormatService) {
        this.databasePort = databasePort;
        this.bundleFormatService = bundleFormatService;
    }

    @Override
    public Optional<AlignedBundle> buildAlignedBundle(BuildChartDataParams params) {
        final var chartSeriesParamList = params.chartSeriesParams();
        final var strategy = params.strategy();
        final var macroSeriesList = databasePort.readMacroSeries(chartSeriesParamList);

        return bundleFormatService.align(macroSeriesList, strategy);
    }

    @Override
    public List<SeriesConfig> getSeriesConfigList() {
        return databasePort.readSeriesConfigList();
    }

}
