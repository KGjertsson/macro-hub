package com.kg.macroanalyzer.application.services.bundleformat.timefilter;

import com.kg.macroanalyzer.TestJsonReader;
import com.kg.macroanalyzer.application.services.bundleformat.timefilterstrategy.TimeFilterStrategyAll;
import com.kg.macroanalyzer.application.services.bundleformat.timefilterstrategy.TimeFilterStrategyOneMonth;
import com.kg.macroanalyzer.application.services.bundleformat.timefilterstrategy.TimeFilterStrategyOneYear;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeFilterStrategyTest {

    TestJsonReader testJsonReader;

    @BeforeEach
    public void setUp() {
        testJsonReader = new TestJsonReader();
    }


    @ParameterizedTest
    @ValueSource(strings = {"json/single_macro_series.json"})
    public void shouldReturnAll_whenAllStrategy(String resource) {
        // given
        final var inputSeries = testJsonReader.readMacroSeriesList(resource);
        final var strategy = new TimeFilterStrategyAll();

        // when
        final var response = strategy.filter(inputSeries);

        // then
        assertEquals(inputSeries, response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"json/multiple_years_macro_series.json"})
    public void shouldReturnFiltered_whenOneYearStrategy(String resource) {
        // given
        final var inputSeries = testJsonReader.readMacroSeriesList(resource);
        final var strategy = new TimeFilterStrategyOneYear(LocalDate.of(2025, 9, 1));

        // when
        final var response = strategy.filter(inputSeries);
        final var firstSeries = response.getFirst();
        final var lastSeries = response.getLast();

        // then
        assertEquals(2, firstSeries.macroPoints().size());
        assertEquals(2, lastSeries.macroPoints().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"json/multiple_months_macro_series.json"})
    public void shouldReturnFiltered_whenOneMonthStrategy(String resource) {
        // given
        final var inputSeries = testJsonReader.readMacroSeriesList(resource);
        final var strategy = new TimeFilterStrategyOneMonth(LocalDate.of(2024, 3, 1));

        // when
        final var response = strategy.filter(inputSeries);
        final var firstSeries = response.getFirst();
        final var lastSeries = response.getLast();

        // then
        assertEquals(2, firstSeries.macroPoints().size());
        assertEquals(2, lastSeries.macroPoints().size());
    }

}
