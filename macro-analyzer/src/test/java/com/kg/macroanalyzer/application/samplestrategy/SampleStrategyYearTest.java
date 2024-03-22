package com.kg.macroanalyzer.application.samplestrategy;

import com.kg.macroanalyzer.TestJsonReader;
import com.kg.macroanalyzer.application.services.bundleformat.samplestrategy.SampleStrategyYear;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SampleStrategyYearTest {

    SampleStrategyYear sampleStrategyYear;

    TestJsonReader testJsonReader;

    @BeforeEach
    public void setUp() {
        sampleStrategyYear = new SampleStrategyYear();
        testJsonReader = new TestJsonReader();
    }

    @ParameterizedTest
    @ValueSource(strings = {"json/multiple_macro_series_multiple_years.json"})
    public void testSampleStrategyMonth_multipleSeries(String resource) {
        // given
        final var inputSeries = testJsonReader.readMacroSeriesList(resource);

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

}
