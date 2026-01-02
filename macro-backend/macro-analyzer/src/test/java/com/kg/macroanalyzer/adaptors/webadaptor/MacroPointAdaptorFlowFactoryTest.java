package com.kg.macroanalyzer.adaptors.webadaptor;

import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.MacroPointAdaptorFlowFactory;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.connectionbuilder.EuroStatConnectionBuilder;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.connectionbuilder.FedConnectionBuilder;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.connectionbuilder.RiksbankenConnectionBuilder;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.getter.HttpGetter;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.EuroStatResponseParser;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.FedResponseParser;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.RiksbankenResponseParser;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class MacroPointAdaptorFlowFactoryTest {

    MacroPointAdaptorFlowFactory macroPointAdaptorFlowFactory;

    @BeforeEach
    void setUp() {
        final var fedKey = "fedKey";
        final var riksbankenKey = "riksbankenKey";
        macroPointAdaptorFlowFactory = new MacroPointAdaptorFlowFactory(fedKey, riksbankenKey);
    }

    @Test
    void shouldBuildFedFlow_whenConfigNameStartsWithFED() {
        // given
        final var seriesConfig = SeriesConfig.builder().name("FED_SOMETHING").build();

        // when
        final var result = macroPointAdaptorFlowFactory.build(seriesConfig);

        // then
        assertNotNull(result);
        assertInstanceOf(FedConnectionBuilder.class, result.connectionBuilder());
        assertInstanceOf(HttpGetter.class, result.getter());
        assertInstanceOf(FedResponseParser.class, result.responseParser());
    }

    @Test
    void shouldBuildEuroStatFlow_whenConfigNameStartsWithDebt() {
        // given
        final var seriesConfig = SeriesConfig.builder().name("Debt_SOMETHING").build();

        // when
        final var result = macroPointAdaptorFlowFactory.build(seriesConfig);

        // then
        assertNotNull(result);
        assertInstanceOf(EuroStatConnectionBuilder.class, result.connectionBuilder());
        assertInstanceOf(HttpGetter.class, result.getter());
        assertInstanceOf(EuroStatResponseParser.class, result.responseParser());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Int_something", "Euro_something", "PolicyRateSweden"})
    void shouldBuildRiksbankenFlow_whenConfigNameIsRiksbankenCompatible(String name) {
        // given
        final var seriesConfig = SeriesConfig.builder().name(name).build();

        // when
        final var result = macroPointAdaptorFlowFactory.build(seriesConfig);

        // then
        assertNotNull(result);
        assertInstanceOf(RiksbankenConnectionBuilder.class, result.connectionBuilder());
        assertInstanceOf(HttpGetter.class, result.getter());
        assertInstanceOf(RiksbankenResponseParser.class, result.responseParser());
    }

    @Test
    void shouldThrowIllegalStateException_whenConfigNameIsUnknown() {
        // given
        final var seriesConfig = SeriesConfig.builder().name("UNKNOWN").build();

        // when && then
        final var exception = assertThrows(
                IllegalStateException.class,
                () -> macroPointAdaptorFlowFactory.build(seriesConfig)
        );
        assertEquals("Unexpected value: UNKNOWN", exception.getMessage());
    }
}
