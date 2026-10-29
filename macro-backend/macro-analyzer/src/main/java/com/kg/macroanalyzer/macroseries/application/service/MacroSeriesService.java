package com.kg.macroanalyzer.macroseries.application.service;

import com.kg.macroanalyzer.macroseries.application.port.in.BuildChartDataParams;
import com.kg.macroanalyzer.macroseries.application.port.in.ChartSeriesParam;
import com.kg.macroanalyzer.macroseries.application.port.in.MacroSeriesQueries;
import com.kg.macroanalyzer.macroseries.application.port.out.MacroSeriesRepositoryPort;
import com.kg.macroanalyzer.macroseries.application.service.bundleformat.AlignmentService;
import com.kg.macroanalyzer.macroseries.application.service.bundleformat.TimeFilterService;
import com.kg.macroanalyzer.macroseries.domain.AlignedBundle;
import com.kg.macroanalyzer.macroseries.domain.MacroSeries;
import com.kg.macroanalyzer.scrape.application.port.out.SeriesConfigPort;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class MacroSeriesService implements MacroSeriesQueries {

    private static final Set<String> NON_CHART_CATEGORIES = Set.of("Riksdagen", "Valmyndigheten", "SCB");

    private final MacroSeriesRepositoryPort repositoryPort;
    private final AlignmentService alignmentService;
    private final TimeFilterService timeFilterService;
    private final List<SeriesConfig> seriesConfigList;

    @Autowired
    public MacroSeriesService(
            MacroSeriesRepositoryPort repositoryPort,
            SeriesConfigPort seriesConfigPort,
            AlignmentService alignmentService,
            TimeFilterService timeFilterService
    ) {
        this.repositoryPort = repositoryPort;
        this.alignmentService = alignmentService;
        this.timeFilterService = timeFilterService;
        this.seriesConfigList = seriesConfigPort.readSeriesConfigList();
    }

    @Override
    public Optional<AlignedBundle> buildAlignedBundle(@NonNull BuildChartDataParams params) {
        final var strategy = params.strategy();
        final var timeFrame = params.timeFrame();

        return readMacroSeries(params.chartSeriesParams())
                .flatMap(macroSeries -> timeFilterService.shrinkToTimeFrame(macroSeries, timeFrame))
                .flatMap(macroSeriesList -> alignmentService.align(macroSeriesList, strategy));
    }

    @Override
    public List<SeriesConfig> getSeriesConfigList() {
        return seriesConfigList;
    }

    // Riksdagen, Valmyndigheten and SCB scrapes are not time series, so they are kept out of the chart picker
    @Override
    public List<SeriesConfig> getChartSeriesConfigList() {
        return seriesConfigList.stream()
                .filter(c -> !NON_CHART_CATEGORIES.contains(c.category()))
                .toList();
    }

    private Optional<List<MacroSeries>> readMacroSeries(
            @NonNull List<ChartSeriesParam> chartSeriesParamList
    ) {
        final var chartSeriesParamNames = chartSeriesParamList.stream()
                .map(ChartSeriesParam::name)
                .toList();

        final var requestedMacroSeries = seriesConfigList.stream()
                .filter(c -> chartSeriesParamNames.contains(c.name()))
                .toList();

        return Optional.ofNullable(repositoryPort.readMacroSeries(requestedMacroSeries));
    }

}
