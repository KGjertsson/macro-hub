package com.kg.macroanalyzer.macroseries.application.service.bundleformat;

import com.kg.macroanalyzer.TestJsonReader;
import com.kg.macroanalyzer.macroseries.application.port.in.TimeFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimeFilterTest {

    TestJsonReader testJsonReader;

    @BeforeEach
    public void setUp() {
        testJsonReader = new TestJsonReader();
    }

    @Test
    public void shouldThrowIllegalArgumentException_whenTimeFrameIsNull() {
        // when/then
        assertThrows(
                IllegalArgumentException.class,
                () -> TimeFilter.shrinkToTimeFrame(Collections.emptyList(), null)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"json/multiple_macro_series_different_length.json"})
    public void shouldKeepEverySeries_whenTimeFrameIsAll(String resource) {
        // given
        final var inputSeries = testJsonReader.readMacroSeriesList(resource);

        // when
        final var response = TimeFilter.shrinkToTimeFrame(inputSeries, TimeFrame.ALL);

        // then
        assertEquals(inputSeries, response.orElseThrow());
    }

    @Test
    public void shouldReturnEmpty_whenSeriesListIsNull() {
        // when
        final var response = TimeFilter.shrinkToTimeFrame(null, TimeFrame.ALL);

        // then
        assertTrue(response.isEmpty());
    }

}
