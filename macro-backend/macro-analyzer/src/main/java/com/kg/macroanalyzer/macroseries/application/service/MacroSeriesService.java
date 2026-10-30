package com.kg.macroanalyzer.macroseries.application.service;

import com.kg.macroanalyzer.macroseries.application.port.in.BuildChartDataParams;
import com.kg.macroanalyzer.macroseries.application.port.in.ChartSeriesParam;
import com.kg.macroanalyzer.macroseries.application.port.in.MacroSeriesQueries;
import com.kg.macroanalyzer.macroseries.application.port.out.MacroSeriesRepositoryPort;
import com.kg.macroanalyzer.macroseries.application.service.bundleformat.AlignmentService;
import com.kg.macroanalyzer.macroseries.application.service.bundleformat.TimeFilterService;
import com.kg.macroanalyzer.macroseries.domain.AlignedBundle;
import com.kg.macroanalyzer.macroseries.domain.MacroSeries;
import com.kg.macroanalyzer.macroseries.domain.MacroSeriesScrapeFlows;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MacroSeriesService implements MacroSeriesQueries {

    private final MacroSeriesRepositoryPort repositoryPort;
    private final AlignmentService alignmentService;
    private final TimeFilterService timeFilterService;
    private final List<SeriesConfig> seriesConfigList;

    @Autowired
    public MacroSeriesService(
            MacroSeriesRepositoryPort repositoryPort,
            List<SeriesConfig> seriesConfigList,
            AlignmentService alignmentService,
            TimeFilterService timeFilterService
    ) {
        this.repositoryPort = repositoryPort;
        this.alignmentService = alignmentService;
        this.timeFilterService = timeFilterService;
        this.seriesConfigList = seriesConfigList;
    }

    @Override
    public Optional<AlignedBundle> buildAlignedBundle(@NonNull BuildChartDataParams params) {
        final var strategy = params.strategy();
        final var timeFrame = params.timeFrame();

        return readMacroSeries(params.chartSeriesParams())
                .flatMap(macroSeries -> timeFilterService.shrinkToTimeFrame(macroSeries, timeFrame))
                .flatMap(macroSeriesList -> alignmentService.align(macroSeriesList, strategy));
    }

    // Only configs scraped by the macro point flow are time series; the other domains' configs stay out of the picker
    @Override
    public List<SeriesConfig> getChartSeriesConfigList() {
        return seriesConfigList.stream()
                .filter(c -> MacroSeriesScrapeFlows.MACRO_POINT.equals(c.scrapeFlow()))
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
