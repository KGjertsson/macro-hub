package com.kg.macroanalyzer.application.samplestrategy;

import com.kg.macroanalyzer.TestJsonReader;
import com.kg.macroanalyzer.application.services.bundleformat.samplestrategy.SampleStrategyDay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SampleStrategyDayTest {

    SampleStrategyDay sampleStrategyDay;
    TestJsonReader testJsonReader;

    @BeforeEach
    public void setUp() {
        sampleStrategyDay = new SampleStrategyDay();
        testJsonReader = new TestJsonReader();
    }

    @ParameterizedTest
    @ValueSource(strings = {"json/single_macro_series.json"})
    public void sampleMultipleDays_shouldReturnAll(String resource) {
        // given
        final var inputSeries = testJsonReader.readMacroSeriesList(resource);

        // when
        final var sampledBundle = sampleStrategyDay.sample(inputSeries);

        // then
        assertTrue(sampledBundle.isPresent());
        sampledBundle.ifPresent(b -> assertEquals(3, b.macroSeries().getFirst().macroPoints().size()));
    }

}
