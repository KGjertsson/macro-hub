package com.kg.macroanalyzer.application.samplestrategy;

import com.kg.macroanalyzer.TestJsonReader;
import com.kg.macroanalyzer.application.services.bundleformat.samplestrategy.SampleStrategyMonth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.YearMonth;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SampleStrategyMonthTest {

    SampleStrategyMonth sampleStrategyMonth;
    TestJsonReader testJsonReader;

    @BeforeEach
    public void setUp() {
        sampleStrategyMonth = new SampleStrategyMonth();
        testJsonReader = new TestJsonReader();
    }

    @ParameterizedTest
    @ValueSource(strings = {"json/single_macro_series.json"})
    public void sampleOneSeriesMultipleMonths_shouldReturnUniqueResponse(String resource) {
        // given
        final var inputSeries = testJsonReader.readMacroSeriesList(resource);

        // when
        final var sampledBundle = sampleStrategyMonth.sample(inputSeries);

        // then
        final var expectedSeriesSize = 2;

        assertTrue(sampledBundle.isPresent());
        sampledBundle.ifPresent(presentBundle -> {
            final var sampledSeries = presentBundle.macroSeries().getFirst().macroPoints();

            final var firstPoint = inputSeries.getFirst().macroPoints().getFirst();
            final var firstInputAsYearMonth = firstPoint.toBuilder()
                    .date(YearMonth.from(firstPoint.date()))
                    .build();
            final var lastPoint = inputSeries.getFirst().macroPoints().getLast();
            final var lastInputAsYearMonth = lastPoint.toBuilder()
                    .date(YearMonth.from(lastPoint.date()))
                    .build();

            assertEquals(expectedSeriesSize, sampledSeries.size());
            assertEquals(firstInputAsYearMonth, sampledSeries.getFirst());
            assertEquals(lastInputAsYearMonth, sampledSeries.getLast());
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"json/multiple_months_macro_series.json"})
    public void sampleMultipleSeriesMultipleMonths_shouldReturnUniqueResponse(String resource) {
        // given
        final var inputSeries = testJsonReader.readMacroSeriesList(resource);

        // when
        final var sampledBundle = sampleStrategyMonth.sample(inputSeries);

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

    @ParameterizedTest
    @ValueSource(strings = {"json/multiple_macro_series_different_length.json"})
    public void testSampleStrategyMonth_nonNormalizedInput(String resource) {
        // given
        final var inputSeries = testJsonReader.readMacroSeriesList(resource);

        // when
        final var sampledBundle = sampleStrategyMonth.sample(inputSeries);

        // then
        assertTrue(sampledBundle.isEmpty());
    }

}
