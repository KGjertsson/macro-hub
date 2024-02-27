package com.kg.macroanalyzer.adaptors.http;

import com.kg.macroanalyzer.application.ports.driving.BuildChartDataParams;
import com.kg.macroanalyzer.application.ports.driving.ChartData;
import com.kg.macroanalyzer.application.ports.driving.ChartDataWithLabels;
import com.kg.macroanalyzer.application.ports.driving.DrivingPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RestApiAdaptorTest {

    @InjectMocks
    RestApiAdaptor restApiAdaptor;

    @Mock
    DrivingPort drivingPort;

    @Test
    public void testBuildChartData_multipleSeries() {
        // given
        final List<Temporal> cloudBerry = List.of(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 2),
                LocalDate.of(2024, 1, 3)

        );
        final var chartData = List.of(
                ChartData.builder()
                        .name("a")
                        .color("color1")
                        .values(List.of(1.0, 2.0, 3.0))
                        .build(),
                ChartData.builder()
                        .name("b")
                        .color("color2")
                        .values(List.of(4.0, 5.0, 6.0))
                        .build()
        );
        final var chartDataWithLabels = ChartDataWithLabels.builder()
                .labels(cloudBerry)
                .chartData(chartData)
                .build();

        when(drivingPort.buildAlignedBundle(any())).thenReturn(Optional.of(chartDataWithLabels));
        final var response = restApiAdaptor.toChartDataWithLabels(BuildChartDataParams.builder().build());

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(chartDataWithLabels, response.getBody());
    }

    @Test
    public void testBuildChartData_emptyParams() {
        // given
        final var params = BuildChartDataParams.builder().build();

        // when
        when(drivingPort.buildAlignedBundle(any())).thenReturn(Optional.empty());
        final var response = restApiAdaptor.toChartDataWithLabels(params);

        // then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

}
