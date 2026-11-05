package com.kg.macroanalyzer.macroseries.application.service.bundleformat.samplestrategy;

import com.kg.macroanalyzer.macroseries.application.port.in.Strategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StrategyFactoryTest {

    @ParameterizedTest
    @NullSource
    public void buildShouldThrowIllegalArgumentException_whenNullStrategy(Strategy strategy) {
        // when/then
        assertThrows(
                IllegalArgumentException.class,
                () -> StrategyFactory.build(strategy)
        );
    }

    @Test
    public void shouldBuildSampleStrategyMonth_whenMonthStrategy() {
        // given
        final var strategy = Strategy.MONTH;

        // when
        final var result = StrategyFactory.build(strategy);

        // then
        assertEquals(SampleStrategyMonth.class, result.getClass());
    }

    @Test
    public void shouldBuildSampleStrategyYear_whenYearStrategy() {
        // given
        final var strategy = Strategy.YEAR;

        // when
        final var result = StrategyFactory.build(strategy);

        // then
        assertEquals(SampleStrategyYear.class, result.getClass());
    }

}
