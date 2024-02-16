package com.kg.macroanalyzer.application;

import com.kg.macroanalyzer.application.domain.MacroBundle;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.domain.MacroSeries;
import com.kg.macroanalyzer.application.samplestrategy.SampleStrategyMonth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SampleStrategyMonthTest {

    SampleStrategyMonth sampleStrategyMonth;

    @BeforeEach
    public void setUp() {
        sampleStrategyMonth = new SampleStrategyMonth();
    }

    @Test
    public void testSampleStrategyMonth_multipleMonths() {
        // given
        final var inputSeries = createMacroSeries("a");
        final var inputBundle = new MacroBundle(List.of(inputSeries));

        // when
        final var sampledBundle = sampleStrategyMonth.sample(inputBundle);

        // then
        final var expectedSeriesSize = 2;

        assertTrue(sampledBundle.isPresent());
        sampledBundle.ifPresent(presentBundle -> {
            final var sampledSeries = presentBundle.macroSeries().getFirst().macroPoints();

            final var firstPoint = inputSeries.macroPoints().getFirst();
            final var firstInputAsYearMonth = firstPoint.toBuilder()
                    .date(YearMonth.from(firstPoint.date()))
                    .build();
            final var lastPoint = inputSeries.macroPoints().getLast();
            final var lastInputAsYearMonth = lastPoint.toBuilder()
                    .date(YearMonth.from(lastPoint.date()))
                    .build();

            assertEquals(expectedSeriesSize, sampledSeries.size());
            assertEquals(firstInputAsYearMonth, sampledSeries.getFirst());
            assertEquals(lastInputAsYearMonth, sampledSeries.getLast());
        });
    }

    @Test
    public void testSampleStrategyMonth_multipleSeries() {
        // given
        final var inputSeries = List.of(createMacroSeries("a"), createMacroSeries("b"));
        final var inputBundle = new MacroBundle(inputSeries);

        // when
        final var sampledBundle = sampleStrategyMonth.sample(inputBundle);

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

    @Test
    public void testSampleStrategyMonth_emptySeries() {
        // given
        final var inputBundle = new MacroBundle(List.of());

        // when
        final var sampledBundle = sampleStrategyMonth.sample(inputBundle);

        // then
        assertTrue(sampledBundle.isEmpty());
    }

    @Test
    public void testSampleStrategyMonth_nullInput() {
        // when
        final var sampledBundle = sampleStrategyMonth.sample(null);

        // then
        assertTrue(sampledBundle.isEmpty());
    }

    @Test
    public void testSampleStrategyMonth_nonNormalizedInput() {
        // given
        final var seriesOne = createMacroSeries("a");
        var seriesTwo = createMacroSeries("b");
        seriesTwo = seriesTwo.toBuilder()
                .macroPoints(seriesTwo.macroPoints().subList(0, seriesTwo.macroPoints().size() - 2))
                .build();

        final var inputBundle = new MacroBundle(List.of(seriesOne, seriesTwo));

        // when
        final var sampledBundle = sampleStrategyMonth.sample(inputBundle);

        // then
        assertTrue(sampledBundle.isEmpty());
    }

    private MacroSeries createMacroSeries(String name) {
        final var inputPointsOne = List.of(
                new MacroPoint(202411.0, LocalDate.of(2024, 1, 1)),
                new MacroPoint(202412.0, LocalDate.of(2024, 1, 2)),
                new MacroPoint(202421.0, LocalDate.of(2024, 2, 1))
        );
        return new MacroSeries(name, inputPointsOne);
    }

}
