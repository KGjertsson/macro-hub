package com.kg.macroanalyzer.application.samplestrategy;

import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.services.bundleformat.samplestrategy.SampleStrategyYear;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SampleStrategyYearTest {

    SampleStrategyYear sampleStrategyYear;

    @BeforeEach
    public void setUp() {
        sampleStrategyYear = new SampleStrategyYear();
    }

    @Test
    public void testSampleStrategyMonth_multipleSeries() {
        // given
        final var inputSeries = List.of(createMacroSeries("a"), createMacroSeries("b"));

        // when
        final var sampledBundle = sampleStrategyYear.sample(inputSeries);

        // then
        final var expectedSeriesSize = 2;

        assertTrue(sampledBundle.isPresent());
        sampledBundle.ifPresent(presentBundle -> {
            assertEquals(expectedSeriesSize, presentBundle.macroSeries().size());

            IntStream.range(0, presentBundle.macroSeries().size()).forEach(index -> {
                final var indexedInput = presentBundle.macroSeries().get(index).macroPoints();
                final var indexedSampled = presentBundle.macroSeries().get(index).macroPoints();

                assertEquals(expectedSeriesSize, indexedSampled.size());
                assertEquals(indexedInput.getFirst(), indexedSampled.getFirst());
                assertEquals(indexedInput.getLast(), indexedSampled.getLast());
            });
        });
    }

    private MacroSeries createMacroSeries(String name) {
        final var inputPointsOne = List.of(
                new MacroPoint(202411.0, LocalDate.of(2024, 1, 1)),
                new MacroPoint(202512.0, LocalDate.of(2025, 1, 2)),
                new MacroPoint(202621.0, LocalDate.of(2025, 2, 1))
        );
        return new MacroSeries(name, inputPointsOne);
    }

}
