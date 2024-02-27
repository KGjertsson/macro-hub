package com.kg.macroanalyzer.adaptors.http;

import com.kg.macroanalyzer.application.domain.AlignedBundle;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.ports.driving.BuildChartDataParams;
import com.kg.macroanalyzer.application.ports.driving.ChartData;
import com.kg.macroanalyzer.application.ports.driving.ChartDataWithLabels;
import com.kg.macroanalyzer.application.ports.driving.DrivingPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("macro-analyzer")
public class RestApiAdaptor {

    private final DrivingPort drivingPort;

    @Autowired
    public RestApiAdaptor(DrivingPort drivingPort) {
        this.drivingPort = drivingPort;
    }

    @PostMapping("/chart-data")
    public ResponseEntity<ChartDataWithLabels> toChartDataWithLabels(
            @RequestBody BuildChartDataParams params
    ) {
        logRequest(params);
        if (invalidRequest(params)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return drivingPort.buildAlignedBundle(params)
                .flatMap(this::toChartDataWithLabels)
                .map(this::toResponseEntity)
                .orElse(noContentResponse());
    }

    private void logRequest(BuildChartDataParams params) {
        final var msgRaw = "Received GET chart-values request with chartSeriesParams=%s";
        final var msgFormatted = msgRaw.formatted(params);
        log.info(msgFormatted);
    }

    private boolean invalidRequest(BuildChartDataParams params) {
        if (params.strategy() == null) {
            log.error("Received bad request with strategy == null");
            return true;
        }
        if (params.chartSeriesParams() == null) {
            log.error("Received bad request with chartSeriesParams == null");
            return true;
        }

        return false;
    }

    private Optional<ChartDataWithLabels> toChartDataWithLabels(AlignedBundle alignedBundle) {
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
        return macroSeriesList.stream()
                .map(this::buildChartData)
                .toList();
    }

    private ChartData buildChartData(MacroSeries macroSeries) {
        return ChartData.builder()
                .name(macroSeries.name())
                .values(macroSeries.getValues())
                .color("rgb(207,82,48)")
                .build();
    }

    private ResponseEntity<ChartDataWithLabels> toResponseEntity(
            ChartDataWithLabels chartDataWithLabels
    ) {
        return new ResponseEntity<>(chartDataWithLabels, HttpStatus.OK);
    }

    private ResponseEntity<ChartDataWithLabels> noContentResponse() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
