package com.kg.macroanalyzer.application.services.bundleformat;

import com.kg.macroanalyzer.application.domain.AlignedBundle;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.services.LabelGenerationService;
import com.kg.macroanalyzer.application.services.bundleformat.samplestrategy.SampleStrategy;
import com.kg.macroanalyzer.application.services.bundleformat.samplestrategy.StrategyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AlignmentServiceTest {

    private final StrategyFactory.Strategy STRATEGY = StrategyFactory.Strategy.MONTH;

    @InjectMocks
    AlignmentService alignmentService;
    @Mock
    StrategyFactory strategyFactory;
    @Mock
    LabelGenerationService labelGenerationService;
    @Mock
    SampleStrategy sampleStrategy;


    @Test
    public void alignBundle_withSimpleBundle() {
        // given
        final var macroSeries = List.of(
                MacroSeries.builder()
                        .name("a")
                        .macroPoints(emptyPoints())
                        .build()
        );
        final var labels = macroSeries.getFirst()
                .macroPoints().stream()
                .map(MacroPoint::date).toList();
        final var inputBundle = AlignedBundle.builder()
                .macroSeries(macroSeries)
                .labels(labels)
                .build();
        when(labelGenerationService.padToFullLabels(macroSeries)).thenReturn(Optional.of(macroSeries));
        when(strategyFactory.build(STRATEGY)).thenReturn(sampleStrategy);
        when(sampleStrategy.sample(macroSeries)).thenReturn(Optional.of(inputBundle));

        // when
        final var result = alignmentService.align(macroSeries, STRATEGY);

        // then
        assertTrue(result.isPresent());
        result.ifPresent(r -> assertEquals(r, inputBundle));
    }

    @Test
    public void alignBundle_withNullBundle() {
        // given
        when(strategyFactory.build(STRATEGY)).thenReturn(sampleStrategy);

        // when
        final var result = alignmentService.align(null, STRATEGY);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldThrowException_whenStrategyIsNull() {
        // given
        when(strategyFactory.build(null)).thenThrow(IllegalArgumentException.class);

        // when/then
        assertThrows(
                IllegalArgumentException.class,
                () -> alignmentService.align(emptyList(), null)
        );
    }

    private List<MacroPoint> emptyPoints() {
        return IntStream.range(0, 1)
                .mapToObj(Double::valueOf)
                .map(n -> MacroPoint.builder().value(n).date(LocalDate.MIN).build())
                .toList();
    }

}
