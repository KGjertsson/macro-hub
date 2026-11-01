package com.kg.macroanalyzer.macroseries.adaptors.http;

import com.kg.macroanalyzer.macroseries.application.port.in.BuildChartDataParams;
import com.kg.macroanalyzer.macroseries.application.port.in.ChartData;
import com.kg.macroanalyzer.macroseries.application.port.in.ChartDataWithLabels;
import com.kg.macroanalyzer.macroseries.application.port.in.ChartSeriesParam;
import com.kg.macroanalyzer.macroseries.application.port.in.MacroSeriesQueries;
import com.kg.macroanalyzer.macroseries.application.port.in.Strategy;
import com.kg.macroanalyzer.macroseries.application.port.in.TimeFrame;
import com.kg.macroanalyzer.macroseries.domain.AlignedBundle;
import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import com.kg.macroanalyzer.macroseries.domain.MacroSeries;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MacroSeriesControllerTest {

    @InjectMocks
    MacroSeriesController macroSeriesController;

    @Mock
    MacroSeriesQueries macroSeriesQueries;
    @Mock
    ColorSelectionStrategy colorSelectionStrategy;

    @Test
    public void shouldReturnOk_whenValidBundleRetrieved() {
        // given
        final List<Temporal> labels = List.of(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 2),
                LocalDate.of(2024, 1, 3)
        );
        final List<MacroPoint> macroPoints = List.of(
                new MacroPoint(1.0, labels.get(0)),
                new MacroPoint(2.0, labels.get(1)),
                new MacroPoint(3.0, labels.get(2))
        );
        final var macroSeries = List.of(
                new MacroSeries("seriesA", macroPoints)
        );
        final var alignedBundle = AlignedBundle.builder()
                .labels(labels)
                .macroSeries(macroSeries)
                .build();
        final var chartData = macroSeries.stream()
                .map(s -> new ChartData(s.getValues(), s.name(), "rgb(207,82,48)"))
                .toList();
        final var expected = ChartDataWithLabels.builder()
                .labels(labels)
                .chartData(chartData)
                .build();

        // when
        final var params = BuildChartDataParams.builder()
                .strategy(Strategy.MONTH)
                .timeFrame(TimeFrame.ALL)
                .chartSeriesParams(List.of())
                .build();
        when(macroSeriesQueries.buildAlignedBundle(any())).thenReturn(Optional.of(alignedBundle));
        when(colorSelectionStrategy.pickColor(any())).thenReturn("rgb(207,82,48)");
        final var response = macroSeriesController.buildChartData(params);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    public void shouldReturnBadRequest_whenEmptyRequest() {
        // given
        final var params = BuildChartDataParams.builder().build();

        // when
        final var response = macroSeriesController.buildChartData(params);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void shouldReturnBadRequest_whenStrategyMissing() {
        // given
        final var params = BuildChartDataParams.builder()
                .timeFrame(TimeFrame.ALL)
                .chartSeriesParams(List.of())
                .build();

        // when
        final var response = macroSeriesController.buildChartData(params);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(macroSeriesQueries);
    }

    @Test
    public void shouldReturnBadRequest_whenTimeFrameMissing() {
        // given
        final var params = BuildChartDataParams.builder()
                .strategy(Strategy.MONTH)
                .chartSeriesParams(List.of())
                .build();

        // when
        final var response = macroSeriesController.buildChartData(params);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(macroSeriesQueries);
    }

    @Test
    public void shouldReturnBadRequest_whenChartSeriesParamsMissing() {
        // given
        final var params = BuildChartDataParams.builder()
                .strategy(Strategy.MONTH)
                .timeFrame(TimeFrame.ALL)
                .build();

        // when
        final var response = macroSeriesController.buildChartData(params);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(macroSeriesQueries);
    }

    @Test
    public void shouldReturnBadRequest_whenChartSeriesParamsContainsNull() {
        // given
        final var params = BuildChartDataParams.builder()
                .strategy(Strategy.MONTH)
                .timeFrame(TimeFrame.ALL)
                .chartSeriesParams(Arrays.asList((ChartSeriesParam) null))
                .build();

        // when
        final var response = macroSeriesController.buildChartData(params);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(macroSeriesQueries);
    }

    @Test
    public void shouldReturnNoContent_whenEmptyReplyFromServer() {
        // given
        final var params = BuildChartDataParams.builder()
                .strategy(Strategy.MONTH)
                .timeFrame(TimeFrame.ALL)
                .chartSeriesParams(List.of())
                .build();

        // when
        when(macroSeriesQueries.buildAlignedBundle(any())).thenReturn(Optional.empty());
        final var response = macroSeriesController.buildChartData(params);

        // then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

}
