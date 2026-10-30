package com.kg.macroanalyzer.macroseries.adaptors.http;

import com.kg.macroanalyzer.macroseries.application.port.in.BuildChartDataParams;
import com.kg.macroanalyzer.macroseries.application.port.in.ChartData;
import com.kg.macroanalyzer.macroseries.application.port.in.ChartDataWithLabels;
import com.kg.macroanalyzer.macroseries.application.port.in.MacroSeriesQueries;
import com.kg.macroanalyzer.macroseries.domain.AlignedBundle;
import com.kg.macroanalyzer.macroseries.domain.MacroSeries;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("macro-analyzer")
public class MacroSeriesController {

    private final MacroSeriesQueries macroSeriesQueries;
    private final ColorSelectionStrategy colorSelectionStrategy;

    @Autowired
    public MacroSeriesController(MacroSeriesQueries macroSeriesQueries, ColorSelectionStrategy colorSelectionStrategy) {
        this.macroSeriesQueries = macroSeriesQueries;
        this.colorSelectionStrategy = colorSelectionStrategy;
    }

    @PostMapping("/chart-data")
    public ResponseEntity<ChartDataWithLabels> buildChartData(
            @RequestBody BuildChartDataParams params
    ) {
        logBuildChartDataRequest(params);
        if (invalidRequest(params)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return macroSeriesQueries.buildAlignedBundle(params)
                .flatMap(this::buildChartData)
                .map(this::toResponseEntity)
                .orElse(noContentResponse());
    }

    @GetMapping("/series-config")
    public ResponseEntity<List<SeriesConfig>> getSeriesConfig() {
        log.info("Received GET macro-analyzer/series-config");
        final var seriesConfigList = macroSeriesQueries.getChartSeriesConfigList();
        final var responseCode = seriesConfigList.isEmpty()
                ? HttpStatus.NO_CONTENT
                : HttpStatus.OK;

        return new ResponseEntity<>(seriesConfigList, responseCode);
    }

    private void logBuildChartDataRequest(BuildChartDataParams params) {
        var msg = "Received POST macro-analyzer/chart-data request with ";
        msg = msg + "chartSeriesParams=%s";
        msg = msg.formatted(params);
        log.info(msg);
    }

    private boolean invalidRequest(BuildChartDataParams params) {
        if (params.strategy() == null) {
            log.error("Received bad request with strategy == null");
            return true;
        }
        if (params.timeFrame() == null) {
            log.error("Received bad request with timeFrame == null");
            return true;
        }
        if (params.chartSeriesParams() == null) {
            log.error("Received bad request with chartSeriesParams == null");
            return true;
        }
        if (params.chartSeriesParams().stream().anyMatch(Objects::isNull)) {
            log.error("Received bad request with a null element in chartSeriesParams");
            return true;
        }

        return false;
    }

    private Optional<ChartDataWithLabels> buildChartData(AlignedBundle alignedBundle) {
        return Optional.ofNullable(alignedBundle)
                .map(b -> {
                    final var labels = b.labels();
                    final var chartData = buildChartDataList(b.macroSeries());

                    return ChartDataWithLabels.builder()
                            .labels(labels)
                            .chartData(chartData)
                            .build();
                });
    }

    private List<ChartData> buildChartDataList(List<MacroSeries> macroSeriesList) {
        final var chartDataFactory = new ChartDataFactory();

        return macroSeriesList.stream()
                .map(chartDataFactory::build)
                .toList();
    }

    private ResponseEntity<ChartDataWithLabels> toResponseEntity(
            ChartDataWithLabels chartDataWithLabels
    ) {
        return new ResponseEntity<>(chartDataWithLabels, HttpStatus.OK);
    }

    private ResponseEntity<ChartDataWithLabels> noContentResponse() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private class ChartDataFactory {

        private Integer index;

        public ChartDataFactory() {
            this.index = 0;
        }

        public ChartData build(MacroSeries macroSeries) {
            final var color = colorSelectionStrategy.pickColor(index);
            this.index = this.index + 1;

            return ChartData.builder()
                    .values(macroSeries.getValues())
                    .name(macroSeries.name())
                    .color(color)
                    .build();
        }

    }

}
