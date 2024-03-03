package com.kg.macroanalyzer.application.ports.driving;


import com.kg.macroanalyzer.application.services.bundleformat.BundleFormatService;
import com.kg.macroanalyzer.application.domain.AlignedBundle;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.ports.driven.DatabasePort;
import com.kg.macroanalyzer.application.ports.driving.out.OutPortImpl;
import com.kg.macroanalyzer.application.ports.driving.out.chartdata.BuildChartDataParams;
import com.kg.macroanalyzer.application.services.bundleformat.samplestrategy.StrategyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OutPortImplTest {

    @InjectMocks
    OutPortImpl outPortImpl;

    @Mock
    DatabasePort databasePort;
    @Mock
    BundleFormatService bundleFormatService;

    @Test
    void shouldReturnAlignedBundle_whenInputListIsPresent() {
        // given
        final var firstDate = LocalDate.of(2024, 1, 1);
        final var secondDate = LocalDate.of(2024, 1, 1);
        final var macroSeriesList = List.of(
                new MacroSeries("seriesA", List.of(
                        new MacroPoint(1.0, firstDate),
                        new MacroPoint(2.0, secondDate))
                )
        );
        final var alignedBundle = AlignedBundle.builder()
                .labels(List.of(firstDate, secondDate))
                .macroSeries(macroSeriesList)
                .build();
        when(databasePort.readMacroSeries(any())).thenReturn(emptyList());
        when(bundleFormatService.align(any(), any())).thenReturn(Optional.of(alignedBundle));
        final var params = BuildChartDataParams.builder()
                .chartSeriesParams(emptyList())
                .strategy(StrategyFactory.Strategy.MONTH)
                .build();

        // when

        final var response = outPortImpl.buildAlignedBundle(params);

        // then
        assertTrue(response.isPresent());
        response.ifPresent(r -> {
            assertEquals(2, r.labels().size());
            assertEquals(2, r.macroSeries().getFirst().macroPoints().size());
        });

    }

    @Test
    void shouldReturnEmpty_whenInputListIsEmpty() {
        // given
        final var params = BuildChartDataParams.builder()
                .chartSeriesParams(emptyList())
                .strategy(StrategyFactory.Strategy.MONTH)
                .build();
        when(databasePort.readMacroSeries(params.chartSeriesParams())).thenReturn(emptyList());
        when(bundleFormatService.align(any(), eq(params.strategy()))).thenReturn(Optional.empty());

        // when
        final var response = outPortImpl.buildAlignedBundle(params);

        // then
        assertTrue(response.isEmpty());
    }


}
