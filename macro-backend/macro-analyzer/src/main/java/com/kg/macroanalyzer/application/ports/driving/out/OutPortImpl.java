package com.kg.macroanalyzer.application.ports.driving.out;

import com.kg.macroanalyzer.application.domain.localelection.LocalElectionType;
import com.kg.macroanalyzer.application.domain.macroseries.AlignedBundle;
import com.kg.macroanalyzer.application.domain.macroseries.MacroSeries;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.chartdata.BuildChartDataParams;
import com.kg.macroanalyzer.application.ports.driving.out.chartdata.ChartSeriesParam;
import com.kg.macroanalyzer.application.ports.driving.out.localelection.LocalElectionDetailDto;
import com.kg.macroanalyzer.application.ports.driving.out.localelection.LocalElectionSummaryDto;
import com.kg.macroanalyzer.application.services.bundleformat.AlignmentService;
import com.kg.macroanalyzer.application.services.bundleformat.TimeFilterService;
import com.kg.macroanalyzer.application.services.localelection.LocalElectionService;
import com.kg.macroanalyzer.scrape.application.port.out.SeriesConfigPort;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class OutPortImpl implements OutPort {

    private static final Set<String> NON_CHART_CATEGORIES = Set.of("Riksdagen", "Valmyndigheten", "SCB");

    private final DatabasePort databasePort;
    private final AlignmentService alignmentService;
    private final TimeFilterService timeFilterService;
    private final LocalElectionService localElectionService;
    private final List<SeriesConfig> seriesConfigList;

    @Autowired
    public OutPortImpl(
            DatabasePort databasePort,
            SeriesConfigPort seriesConfigPort,
            AlignmentService alignmentService,
            TimeFilterService timeFilterService,
            LocalElectionService localElectionService
    ) {
        this.databasePort = databasePort;
        this.alignmentService = alignmentService;
        this.timeFilterService = timeFilterService;
        this.localElectionService = localElectionService;
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

    @Override
    public List<LocalElectionSummaryDto> getLocalElectionResults(LocalElectionType type, Integer year) {
        return localElectionService.getResults(type, year);
    }

    @Override
    public Optional<LocalElectionDetailDto> getLocalElectionResult(LocalElectionType type, Integer year, String areaCode) {
        return localElectionService.getResult(type, year, areaCode);
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

        return Optional.ofNullable(databasePort.readMacroSeries(requestedMacroSeries));
    }

}
