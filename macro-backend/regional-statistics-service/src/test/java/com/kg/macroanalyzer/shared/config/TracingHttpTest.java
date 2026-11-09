package com.kg.macroanalyzer.shared.config;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "management.opentelemetry.enabled=true",
        "management.tracing.export.enabled=true",
        "scheduler.enabled=false"
})
@ActiveProfiles("test")
@Import(TracingHttpTest.TestRoutes.class)
class TracingHttpTest {

    private static final String PARENT_SPAN_ID = "b7ad6b7169203331";
    private static final String API_PATH = "/macro-analyzer/regional-statistics/indicators";
    private static final String SERVICE_NAME = "regional-statistics";

    @Autowired
    InMemorySpanExporter spanExporter;

    @Autowired
    SdkTracerProvider tracerProvider;

    @Value("${local.server.port}")
    int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void shouldContinueTheGatewaysTrace_underTheComposeServicesName() throws Exception {
        // given
        final var traceId = newTraceId();

        // when
        get(API_PATH, traceparent(traceId));

        // then
        final var span = awaitSpanOfTrace(traceId);
        assertEquals(PARENT_SPAN_ID, span.getParentSpanId());
        assertEquals(SpanKind.SERVER, span.getKind());
        assertEquals(SERVICE_NAME, span.getResource().getAttribute(AttributeKey.stringKey("service.name")));
    }

    // ECS logging writes the MDC's traceId into every line; only the first context of a JVM decides the log format
    @Test
    void shouldPutTheTraceId_inTheLoggingContextOfTheRequest() throws Exception {
        // given
        final var traceId = newTraceId();

        // when
        final var response = get("/tracing-test", traceparent(traceId));

        // then
        assertEquals(traceId, response.body());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/actuator/prometheus", "/actuator/health"})
    void shouldNotTraceActuator_butKeepItsMetrics(String path) throws Exception {
        // given
        final var actuatorTrace = newTraceId();
        final var laterTrace = newTraceId();

        // when
        final var response = get(path, traceparent(actuatorTrace));
        get(API_PATH, traceparent(laterTrace));
        awaitSpanOfTrace(laterTrace);

        // then
        assertEquals(200, response.statusCode());
        assertTrue(finishedSpans().stream().noneMatch(span -> span.getTraceId().equals(actuatorTrace)), () -> "Spans: " + finishedSpans());
        assertTrue(get("/actuator/prometheus", traceparent(newTraceId())).body().contains("uri=\"" + path + "\""));
    }

    private String traceparent(String traceId) {
        return "00-" + traceId + "-" + PARENT_SPAN_ID + "-01";
    }

    private String newTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private SpanData awaitSpanOfTrace(String traceId) throws InterruptedException {
        final var deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            final var match = finishedSpans().stream().filter(span -> span.getTraceId().equals(traceId)).findFirst();
            if (match.isPresent()) {
                return match.get();
            }
            Thread.sleep(50);
        }
        throw new AssertionError("No span of trace " + traceId + ": " + finishedSpans());
    }

    private List<SpanData> finishedSpans() {
        tracerProvider.forceFlush().join(10, TimeUnit.SECONDS);
        return spanExporter.getFinishedSpanItems();
    }

    private HttpResponse<String> get(String path, String traceparent) throws Exception {
        final var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("traceparent", traceparent)
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @TestConfiguration
    static class TestRoutes {

        @Bean
        InMemorySpanExporter spanExporter() {
            return InMemorySpanExporter.create();
        }

        @Bean
        TracedRoute tracedRoute() {
            return new TracedRoute();
        }

    }

    @RestController
    static class TracedRoute {

        @GetMapping("/tracing-test")
        String traceIdInTheLoggingContext() {
            return MDC.get("traceId");
        }

    }

}
