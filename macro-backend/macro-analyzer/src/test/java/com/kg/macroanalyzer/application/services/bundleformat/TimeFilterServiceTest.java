package com.kg.macroanalyzer.application.services.bundleformat;

import com.kg.macroanalyzer.TestJsonReader;
import com.kg.macroanalyzer.application.services.bundleformat.samplestrategy.SampleStrategyMonth;
import com.kg.macroanalyzer.application.services.bundleformat.timefilterstrategy.TimeFilterStrategy;
import com.kg.macroanalyzer.application.services.bundleformat.timefilterstrategy.TimeFilterStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TimeFilterServiceTest {

    @InjectMocks
    TimeFilterService timeFilterService;
    @Mock
    TimeFilterStrategyFactory timeFilterStrategyFactory;
    @Mock
    TimeFilterStrategy timeFilterStrategy;

    TestJsonReader testJsonReader;

    @BeforeEach
    public void setUp() {
        testJsonReader = new TestJsonReader();
    }

    @Test
    public void shouldThrowIllegalArgumentException_whenStrategyIsNull() {
        // given
        TimeFilterStrategyFactory.TimeFrame timeFrame = null;
        when(timeFilterStrategyFactory.build(timeFrame)).thenThrow(new IllegalArgumentException());

        //then
        assertThrows(
                IllegalArgumentException.class,
                () -> timeFilterService.shrinkToTimeFrame(Collections.emptyList(), timeFrame)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"json/multiple_macro_series_different_length.json"})
    public void filteredShouldBePresent_whenValidInput(String resource) {
        // given
        final var inputSeries = testJsonReader.readMacroSeriesList(resource);
        when(timeFilterStrategyFactory.build(any())).thenReturn(timeFilterStrategy);
        when(timeFilterStrategy.filter(any())).thenAnswer(a -> a.getArgument(0));

        // when
        final var response = timeFilterService.shrinkToTimeFrame(inputSeries, null);

        // then
        verify(timeFilterStrategy).filter(any());
        assertTrue(response.isPresent());
    }

}
