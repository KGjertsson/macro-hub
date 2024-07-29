package com.kg.macroanalyzer.application.services.bundleformat.sampelstrategy;

import com.kg.macroanalyzer.application.services.bundleformat.samplestrategy.SampleStrategyMonth;
import com.kg.macroanalyzer.application.services.bundleformat.samplestrategy.SampleStrategyYear;
import com.kg.macroanalyzer.application.services.bundleformat.samplestrategy.StrategyFactory;
import com.kg.macroanalyzer.application.services.bundleformat.samplestrategy.StrategyFactory.Strategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StrategyFactoryTest {

    StrategyFactory strategyFactory;

    @BeforeEach
    public void setUp() {
        strategyFactory = new StrategyFactory();
    }

    @ParameterizedTest
    @NullSource
    public void buildShouldThrowIllegalArgumentException_whenNullStrategy(Strategy strategy) {
        // when/then
        assertThrows(
                IllegalArgumentException.class,
                () -> strategyFactory.build(strategy)
        );
    }

    @Test
    public void buildShouldThrowIllegalArgumentException_whenDayStrategy() {
        // given
        final var strategy = Strategy.DAY;

        // when/then
        assertThrows(
                IllegalArgumentException.class,
                () -> strategyFactory.build(strategy)
        );
    }

    @Test
    public void shouldBuildSampleStrategyMonth_whenMonthStrategy() {
        // given
        final var strategy = Strategy.MONTH;

        // when
        final var result = strategyFactory.build(strategy);

        // then
        assertEquals(SampleStrategyMonth.class, result.getClass());
    }

    @Test
    public void shouldBuildSampleStrategyYear_whenYearStrategy() {
        // given
        final var strategy = Strategy.YEAR;

        // when
        final var result = strategyFactory.build(strategy);

        // then
        assertEquals(SampleStrategyYear.class, result.getClass());
    }

}
