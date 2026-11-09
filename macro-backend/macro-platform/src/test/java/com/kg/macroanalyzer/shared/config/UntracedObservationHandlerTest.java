package com.kg.macroanalyzer.shared.config;

import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.handler.TracingAwareMeterObservationHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.scheduling.support.ScheduledTaskObservationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UntracedObservationHandlerTest {

    private final ObservationRegistry registry = ObservationRegistry.create();
    private final SimpleMeterRegistry meters = new SimpleMeterRegistry();
    private final List<Observation.Context> traced = new ArrayList<>();

    @BeforeEach
    void setUp() {
        final var tracingHandler = new DefaultTracingObservationHandler(Tracer.NOOP) {
            @Override
            public void onStart(Observation.Context context) {
                traced.add(context);
                super.onStart(context);
            }
        };
        registry.observationConfig()
                .observationHandler(new ObservationHandler.FirstMatchingCompositeObservationHandler(new UntracedObservationHandler(), tracingHandler))
                .observationHandler(new TracingAwareMeterObservationHandler<>(new DefaultMeterObservationHandler(meters), Tracer.NOOP));
    }

    @Test
    void shouldNotTraceAScheduledTask_butKeepItsMetric() throws Exception {
        // given
        final var method = getClass().getDeclaredMethod("setUp");
        final var context = new ScheduledTaskObservationContext(this, method);

        // when
        runObservation("tasks.scheduled.execution", context);

        // then
        assertTrue(traced.isEmpty());
        assertEquals(1, meters.get("tasks.scheduled.execution").timer().count());
    }

    @Test
    void shouldNotTraceActuatorRequests_butKeepTheirMetric() {
        // when
        runObservation("http.server.requests", serverContext("/actuator/prometheus"));

        // then
        assertTrue(traced.isEmpty());
        assertEquals(1, meters.get("http.server.requests").timer().count());
    }

    @Test
    void shouldTraceApiRequests() {
        // when
        runObservation("http.server.requests", serverContext("/macro-analyzer/series-config"));

        // then
        assertEquals(1, traced.size());
    }

    @Test
    void shouldTraceAPathThatOnlyStartsLikeActuator() {
        // when
        runObservation("http.server.requests", serverContext("/actuatorish"));

        // then
        assertEquals(1, traced.size());
    }

    private void runObservation(String name, Observation.Context context) {
        final var observation = Observation.createNotStarted(name, () -> context, registry).start();
        try (var scope = observation.openScope()) {
            observation.lowCardinalityKeyValue("status", "200");
        }
        observation.stop();
    }

    private ServerRequestObservationContext serverContext(String path) {
        return new ServerRequestObservationContext(new MockHttpServletRequest("GET", path), new MockHttpServletResponse());
    }

}
