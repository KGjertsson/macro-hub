package com.kg.macroanalyzer.macroseries.application.service.bundleformat;

import com.kg.macroanalyzer.macroseries.adaptors.http.ColorSelectionStrategy;
import com.kg.macroanalyzer.macroseries.adaptors.http.MacroSeriesController;
import com.kg.macroanalyzer.macroseries.application.port.in.BuildChartDataParams;
import com.kg.macroanalyzer.macroseries.application.port.in.ChartData;
import com.kg.macroanalyzer.macroseries.application.port.in.ChartDataWithLabels;
import com.kg.macroanalyzer.macroseries.application.port.in.MacroSeriesQueries;
import com.kg.macroanalyzer.macroseries.application.service.LabelGenerationService;
import com.kg.macroanalyzer.macroseries.application.service.bundleformat.samplestrategy.StrategyFactory;
import com.kg.macroanalyzer.macroseries.application.service.bundleformat.timefilterstrategy.TimeFilterStrategyFactory;
import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import com.kg.macroanalyzer.macroseries.domain.MacroSeries;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Runs the chart-data path (time filter, alignment, sampling, response) without mocking the bundle formatting
public class ChartDataPipelineTest {

    private static final LocalDate TODAY = LocalDate.now();

    private final TimeFilterService timeFilterService = new TimeFilterService(new TimeFilterStrategyFactory());
    private final AlignmentService alignmentService = new AlignmentService(
            new LabelGenerationService(),
            new StrategyFactory()
    );

    static Stream<Arguments> strategiesAndTimeFrames() {
        return Arrays.stream(StrategyFactory.Strategy.values())
                .flatMap(strategy -> Arrays.stream(TimeFilterStrategyFactory.TimeFrame.values())
                        .filter(timeFrame -> timeFrame != TimeFilterStrategyFactory.TimeFrame.ALL)
                        .map(timeFrame -> Arguments.of(strategy, timeFrame)));
    }

    @ParameterizedTest
    @MethodSource("strategiesAndTimeFrames")
    public void shouldKeepEmptySeriesAsNulls_whenOneSeriesHasNoDataInTimeFrame(
            StrategyFactory.Strategy strategy,
            TimeFilterStrategyFactory.TimeFrame timeFrame
    ) {
        // given
        final var recent = series("recent", TODAY.minusDays(10), TODAY.minusDays(3), TODAY.minusDays(1));
        final var old = series("old", TODAY.minusYears(30), TODAY.minusYears(20));

        // when
        final var chartData = chartData(List.of(recent, old), strategy, timeFrame);

        // then
        assertEquals(2, chartData.size());
        final var recentValues = chartData.get(0).values();
        final var oldValues = chartData.get(1).values();
        assertEquals("old", chartData.get(1).name());
        assertFalse(recentValues.isEmpty());
        assertTrue(recentValues.stream().allMatch(Objects::nonNull));
        assertEquals(recentValues.size(), oldValues.size());
        assertTrue(oldValues.stream().allMatch(Objects::isNull));
    }

    @ParameterizedTest
    @MethodSource("strategiesAndTimeFrames")
    public void shouldAlignSinglePointInTimeFrame(
            StrategyFactory.Strategy strategy,
            TimeFilterStrategyFactory.TimeFrame timeFrame
    ) {
        // given
        final var single = series("single", TODAY.minusYears(20), TODAY.minusDays(1));
        final var old = series("old", TODAY.minusYears(30));

        // when
        final var chartData = chartData(List.of(old, single), strategy, timeFrame);

        // then
        assertEquals(Collections.singletonList(null), chartData.get(0).values());
        assertEquals(List.of(2.0), chartData.get(1).values());
    }

    @ParameterizedTest
    @MethodSource("strategiesAndTimeFrames")
    public void shouldReturnNoContent_whenNoSeriesHasDataInTimeFrame(
            StrategyFactory.Strategy strategy,
            TimeFilterStrategyFactory.TimeFrame timeFrame
    ) {
        // given
        final var old = series("old", TODAY.minusYears(30));
        final var older = series("older", TODAY.minusYears(40));

        // when
        final var response = respond(List.of(old, older), strategy, timeFrame);

        // then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void shouldSendNullOutsideSeriesRange_whenSeriesSpanDifferentPeriods() {
        // given
        final var long_ = series(
                "long",
                LocalDate.of(2020, 1, 1), LocalDate.of(2020, 2, 1), LocalDate.of(2020, 3, 1),
                LocalDate.of(2020, 4, 1), LocalDate.of(2020, 5, 1)
        );
        final var short_ = series("short", LocalDate.of(2020, 2, 15), LocalDate.of(2020, 3, 10));

        // when
        final var chartData = chartData(
                List.of(long_, short_),
                StrategyFactory.Strategy.MONTH,
                TimeFilterStrategyFactory.TimeFrame.ALL
        );

        // then
        assertEquals(List.of(1.0, 2.0, 3.0, 4.0, 5.0), chartData.get(0).values());
        assertEquals(Arrays.asList(null, 1.0, 1.0, null, null), chartData.get(1).values());
    }

    @Test
    public void shouldAssignColors_whenMoreSeriesThanPaletteColors() {
        // given
        final var macroSeries = IntStream.range(0, 12)
                .mapToObj(i -> series("series" + i, TODAY.minusDays(2), TODAY.minusDays(1)))
                .toList();

        // when
        final var chartData = chartData(
                macroSeries,
                StrategyFactory.Strategy.DAY,
                TimeFilterStrategyFactory.TimeFrame.ALL
        );

        // then
        assertEquals(12, chartData.size());
        assertTrue(chartData.stream().map(ChartData::color).allMatch(Objects::nonNull));
    }

    private List<ChartData> chartData(
            List<MacroSeries> macroSeries,
            StrategyFactory.Strategy strategy,
            TimeFilterStrategyFactory.TimeFrame timeFrame
    ) {
        final var response = respond(macroSeries, strategy, timeFrame);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        final var body = Objects.requireNonNull(response.getBody());
        body.chartData().forEach(c -> assertEquals(body.labels().size(), c.values().size()));

        return body.chartData();
    }

    private ResponseEntity<ChartDataWithLabels> respond(
            List<MacroSeries> macroSeries,
            StrategyFactory.Strategy strategy,
            TimeFilterStrategyFactory.TimeFrame timeFrame
    ) {
        final var bundle = timeFilterService.shrinkToTimeFrame(macroSeries, timeFrame)
                .flatMap(filtered -> alignmentService.align(filtered, strategy));
        final var macroSeriesQueries = mock(MacroSeriesQueries.class);
        when(macroSeriesQueries.buildAlignedBundle(any())).thenReturn(bundle);
        final var params = BuildChartDataParams.builder()
                .strategy(strategy)
                .timeFrame(timeFrame)
                .chartSeriesParams(List.of())
                .build();

        return new MacroSeriesController(macroSeriesQueries, new ColorSelectionStrategy()).buildChartData(params);
    }

    private static MacroSeries series(String name, LocalDate... dates) {
        final var points = IntStream.range(0, dates.length)
                .mapToObj(i -> MacroPoint.builder().value(i + 1.0).date(dates[i]).build())
                .toList();

        return MacroSeries.builder().name(name).macroPoints(points).build();
    }

}
