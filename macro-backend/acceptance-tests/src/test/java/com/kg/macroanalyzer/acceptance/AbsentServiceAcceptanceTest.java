package com.kg.macroanalyzer.acceptance;

import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbsentServiceAcceptanceTest {

    @AutoClose
    private static AcceptanceStack stack;

    private static Gateway gateway;

    @BeforeAll
    static void startOnlyParliament() {
        stack = AcceptanceStack.startWith(List.of(BackendService.PARLIAMENT));
        gateway = stack.gateway();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/macro-analyzer/series-config",
            "/macro-analyzer/regional-statistics/indicators",
            "/macro-analyzer/municipalities/elections"
    })
    void theRoutesOfAServiceThatIsntRunningAnswerBadGateway(String path) {
        assertEquals(502, gateway.get(path).statusCode());
    }

    @Test
    void theRestOfTheSystemKeepsWorking() {
        assertEquals(200, gateway.get("/macro-analyzer/parliament/parties").statusCode());
        assertEquals(200, gateway.get("/parliament").statusCode());
        assertEquals(404, gateway.get("/macro-analyzer/unknown").statusCode());
    }

}
