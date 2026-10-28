package com.kg.macroanalyzer.adaptors.http;

import com.kg.macroanalyzer.application.domain.macroseries.AlignedBundle;
import com.kg.macroanalyzer.application.domain.macroseries.MacroSeries;
import com.kg.macroanalyzer.application.ports.driving.out.OutPort;
import com.kg.macroanalyzer.application.ports.driving.out.chartdata.BuildChartDataParams;
import com.kg.macroanalyzer.application.ports.driving.out.chartdata.ChartData;
import com.kg.macroanalyzer.application.ports.driving.out.chartdata.ChartDataWithLabels;
import com.kg.macroanalyzer.application.domain.localelection.LocalElectionType;
import com.kg.macroanalyzer.application.ports.driving.out.localelection.LocalElectionDetailDto;
import com.kg.macroanalyzer.application.ports.driving.out.localelection.LocalElectionSummaryDto;
import com.kg.macroanalyzer.application.ports.driving.out.parliament.*;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("macro-analyzer")
public class ExtractionAdaptor {

    private final OutPort outPort;
    private final ColorSelectionStrategy colorSelectionStrategy;

    @Autowired
    public ExtractionAdaptor(OutPort outPort, ColorSelectionStrategy colorSelectionStrategy) {
        this.outPort = outPort;
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

        return outPort.buildAlignedBundle(params)
                .flatMap(this::buildChartData)
                .map(this::toResponseEntity)
                .orElse(noContentResponse());
    }

    @GetMapping("/series-config")
    public ResponseEntity<List<SeriesConfig>> getSeriesConfig() {
        log.info("Received GET macro-analyzer/series-config");
        final var seriesConfigList = outPort.getChartSeriesConfigList();
        final var responseCode = seriesConfigList.isEmpty()
                ? HttpStatus.NO_CONTENT
                : HttpStatus.OK;

        return new ResponseEntity<>(seriesConfigList, responseCode);
    }

    @GetMapping("/parliament/parties")
    public ResponseEntity<List<PartyDto>> getParties() {
        log.info("Received GET macro-analyzer/parliament/parties");

        return listResponse(outPort.getParties());
    }

    @GetMapping("/parliament/elections")
    public ResponseEntity<List<ElectionResultDto>> getElections() {
        log.info("Received GET macro-analyzer/parliament/elections");

        return listResponse(outPort.getElections());
    }

    @GetMapping("/parliament/composition")
    public ResponseEntity<CompositionDto> getComposition() {
        log.info("Received GET macro-analyzer/parliament/composition");
        final var composition = outPort.getComposition();
        if (composition.parties().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(composition);
    }

    @GetMapping("/parliament/members")
    public ResponseEntity<List<MemberSummaryDto>> getMembersOfParliament(
            @RequestParam(name = "year", required = false) Integer year
    ) {
        log.info("Received GET macro-analyzer/parliament/members?year=%s".formatted(year));

        return listResponse(outPort.getMembersOfParliament(year));
    }

    @GetMapping("/parliament/members/{id}")
    public ResponseEntity<MemberDetailDto> getMemberOfParliament(@PathVariable("id") String id) {
        log.info("Received GET macro-analyzer/parliament/members/%s".formatted(id));

        return outPort.getMemberOfParliament(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/municipalities/elections")
    public ResponseEntity<List<LocalElectionSummaryDto>> getMunicipalityElections(
            @RequestParam(name = "year", required = false) Integer year
    ) {
        log.info("Received GET macro-analyzer/municipalities/elections?year=%s".formatted(year));

        return listResponse(outPort.getLocalElectionResults(LocalElectionType.KF, year));
    }

    @GetMapping("/municipalities/{code}")
    public ResponseEntity<LocalElectionDetailDto> getMunicipality(
            @PathVariable("code") String code,
            @RequestParam(name = "year", required = false) Integer year
    ) {
        log.info("Received GET macro-analyzer/municipalities/%s?year=%s".formatted(code, year));

        return detailResponse(outPort.getLocalElectionResult(LocalElectionType.KF, year, code));
    }

    @GetMapping("/regions/elections")
    public ResponseEntity<List<LocalElectionSummaryDto>> getRegionElections(
            @RequestParam(name = "year", required = false) Integer year
    ) {
        log.info("Received GET macro-analyzer/regions/elections?year=%s".formatted(year));

        return listResponse(outPort.getLocalElectionResults(LocalElectionType.RF, year));
    }

    @GetMapping("/regions/{code}")
    public ResponseEntity<LocalElectionDetailDto> getRegion(
            @PathVariable("code") String code,
            @RequestParam(name = "year", required = false) Integer year
    ) {
        log.info("Received GET macro-analyzer/regions/%s?year=%s".formatted(code, year));

        return detailResponse(outPort.getLocalElectionResult(LocalElectionType.RF, year, code));
    }

    @GetMapping("/constituencies/elections")
    public ResponseEntity<List<LocalElectionSummaryDto>> getConstituencyElections(
            @RequestParam(name = "year", required = false) Integer year
    ) {
        log.info("Received GET macro-analyzer/constituencies/elections?year=%s".formatted(year));

        return listResponse(outPort.getLocalElectionResults(LocalElectionType.RD, year));
    }

    @GetMapping("/constituencies/{code}")
    public ResponseEntity<LocalElectionDetailDto> getConstituency(
            @PathVariable("code") String code,
            @RequestParam(name = "year", required = false) Integer year
    ) {
        log.info("Received GET macro-analyzer/constituencies/%s?year=%s".formatted(code, year));

        return detailResponse(outPort.getLocalElectionResult(LocalElectionType.RD, year, code));
    }

    private static <T> ResponseEntity<T> detailResponse(Optional<T> detail) {
        return detail
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private static <T> ResponseEntity<List<T>> listResponse(List<T> list) {
        final var responseCode = list.isEmpty()
                ? HttpStatus.NO_CONTENT
                : HttpStatus.OK;

        return new ResponseEntity<>(list, responseCode);
    }

    private void logBuildChartDataRequest(BuildChartDataParams params) {
        var msg = "Received POST macro-analyzer/chart-values request with ";
        msg = msg + "chartSeriesParams=%s";
        msg = msg.formatted(params);
        log.info(msg);
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
