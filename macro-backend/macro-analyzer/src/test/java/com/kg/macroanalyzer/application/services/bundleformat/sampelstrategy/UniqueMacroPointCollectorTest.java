package com.kg.macroanalyzer.application.services.bundleformat.sampelstrategy;

import com.kg.macroanalyzer.application.domain.macroseries.MacroPoint;
import com.kg.macroanalyzer.application.services.bundleformat.samplestrategy.UniqueMacroPointCollector;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UniqueMacroPointCollectorTest {

    private static final LocalDate DAY_1 = LocalDate.of(2020, 1, 1);
    private static final LocalDate DAY_2 = LocalDate.of(2020, 1, 2);
    private static final LocalDate DAY_3 = LocalDate.of(2020, 1, 3);

    @Test
    public void shouldKeepEncounterOrder_whenDatesAreUnique() {
        // given
        final var points = List.of(point(DAY_3, 3.0), point(DAY_1, 1.0), point(DAY_2, 2.0));

        // when
        final var result = points.stream().collect(new UniqueMacroPointCollector());

        // then
        assertEquals(points, result);
    }

    @Test
    public void shouldKeepFirstPointInItsPosition_whenDateRepeats() {
        // given
        final var points = List.of(
                point(DAY_1, 1.0),
                point(DAY_2, 2.0),
                point(DAY_1, 10.0),
                point(DAY_3, 3.0),
                point(DAY_2, 20.0)
        );

        // when
        final var result = points.stream().collect(new UniqueMacroPointCollector());

        // then
        assertEquals(List.of(point(DAY_1, 1.0), point(DAY_2, 2.0), point(DAY_3, 3.0)), result);
    }

    @Test
    public void shouldReplacePaddingInItsPosition_whenLaterPointWithSameDateHasValue() {
        // given
        final var points = List.of(
                point(DAY_1, null),
                point(DAY_2, 2.0),
                point(DAY_1, 1.0),
                point(DAY_1, 10.0)
        );

        // when
        final var result = points.stream().collect(new UniqueMacroPointCollector());

        // then
        assertEquals(List.of(point(DAY_1, 1.0), point(DAY_2, 2.0)), result);
    }

    @Test
    public void shouldKeepPadding_whenNoPointWithSameDateHasValue() {
        // given
        final var points = List.of(point(DAY_1, null), point(DAY_1, null), point(DAY_2, 2.0));

        // when
        final var result = points.stream().collect(new UniqueMacroPointCollector());

        // then
        assertEquals(List.of(point(DAY_1, null), point(DAY_2, 2.0)), result);
    }

    @Test
    public void shouldNotReplaceValue_whenLaterPointWithSameDateIsPadding() {
        // given
        final var points = List.of(point(DAY_1, 1.0), point(DAY_1, null));

        // when
        final var result = points.stream().collect(new UniqueMacroPointCollector());

        // then
        assertEquals(List.of(point(DAY_1, 1.0)), result);
    }

    @Test
    public void shouldDeduplicateSampledDates_whenDatesAreYearMonths() {
        // given
        final var january = YearMonth.of(2020, 1);
        final var february = YearMonth.of(2020, 2);
        final var points = List.of(
                point(january, null),
                point(january, 1.0),
                point(february, 2.0),
                point(february, 20.0)
        );

        // when
        final var result = points.stream().collect(new UniqueMacroPointCollector());

        // then
        assertEquals(List.of(point(january, 1.0), point(february, 2.0)), result);
    }

    @Test
    public void shouldMergeWithSameRules_whenCombiningPartialResults() {
        // given
        final var collector = new UniqueMacroPointCollector();
        final var left = collector.supplier().get();
        final var right = collector.supplier().get();
        Stream.of(point(DAY_1, null), point(DAY_2, 2.0))
                .forEach(p -> collector.accumulator().accept(left, p));
        Stream.of(point(DAY_3, 3.0), point(DAY_1, 1.0), point(DAY_2, 20.0))
                .forEach(p -> collector.accumulator().accept(right, p));

        // when
        final var result = collector.finisher().apply(collector.combiner().apply(left, right));

        // then
        assertEquals(List.of(point(DAY_1, 1.0), point(DAY_2, 2.0), point(DAY_3, 3.0)), result);
    }

    @Test
    public void shouldMatchSequentialResult_whenCollectedInParallel() {
        // given
        final var points = IntStream.range(0, 20_000)
                .mapToObj(i -> point(DAY_1.plusDays(i % 5_000), i < 5_000 && i % 3 == 0 ? null : (double) i))
                .toList();

        // when
        final var sequential = points.stream().collect(new UniqueMacroPointCollector());
        final var parallel = points.parallelStream().collect(new UniqueMacroPointCollector());

        // then
        assertEquals(5_000, sequential.size());
        assertEquals(sequential, parallel);
    }

    private static MacroPoint point(Temporal date, Double value) {
        return MacroPoint.builder()
                .date(date)
                .value(value)
                .build();
    }

}
