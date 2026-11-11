package com.kg.macroanalyzer.shared.config;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
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
import org.springframework.dao.DataAccessException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "management.opentelemetry.enabled=true",
        "management.tracing.export.enabled=true",
        "scheduler.enabled=false"
})
@ActiveProfiles("test")
@Import(TracingHttpTest.TestRoutes.class)
class TracingHttpTest {

    private static final String ADMIN_TOKEN = "admin-token";
    private static final String PARENT_SPAN_ID = "b7ad6b7169203331";
    private static final String API_PATH = "/macro-analyzer/regional-statistics/indicators";
    private static final String SERVICE_NAME = "regional-statistics";
    private static final String QUERY_PATH = "/tracing-test/query";
    private static final String FAILING_QUERY_PATH = "/tracing-test/failing-query";
    private static final String SCRAPE_QUERY_PATH =
            "/macro-analyzer/scrape/regional-statistics/tracing-test";
    private static final String QUERY_VALUE = "value-that-must-not-reach-a-span";

    @Autowired
    InMemorySpanExporter spanExporter;

    @Autowired
    SdkTracerProvider tracerProvider;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Value("${local.server.port}")
    int port;

    @Autowired
    DSLContext dsl;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void shouldContinueTheGatewaysTrace_underTheComposeServicesName() throws Exception {
        // given
        final var traceId = newTraceId();

        // when
        get(API_PATH, traceparent(traceId));

        // then
        final var span = awaitServerSpanOfTrace(traceId);
        assertEquals(PARENT_SPAN_ID, span.getParentSpanId());
        assertEquals(SpanKind.SERVER, span.getKind());
        assertEquals(
                SERVICE_NAME,
                span.getResource().getAttribute(AttributeKey.stringKey("service.name"))
        );
    }

    // ECS logging writes the MDC's traceId into every line; only the first context of a JVM decides
    // the log format
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
        awaitServerSpanOfTrace(laterTrace);

        // then
        assertEquals(200, response.statusCode());
        assertTrue(
                finishedSpans().stream().noneMatch(span -> span.getTraceId().equals(actuatorTrace)),
                () -> "Spans: " + finishedSpans()
        );
        assertTrue(get("/actuator/prometheus", traceparent(newTraceId()))
                .body().contains("uri=\"" + path + "\""));
    }

    @Test
    void shouldAddAChildSpanForAQuery_namedAfterItsTable_withoutItsValues() throws Exception {
        // given
        final var traceId = newTraceId();

        // when
        get(QUERY_PATH, traceparent(traceId));

        // then
        final var server = awaitServerSpanOfTrace(traceId);
        final var query = querySpanOfTrace(traceId);
        final var text = query.getAttributes().get(AttributeKey.stringKey("db.query.text"));
        assertEquals("select series_configuration", query.getName());
        assertEquals(server.getSpanId(), query.getParentSpanId());
        assertEquals(
                "postgresql",
                query.getAttributes().get(AttributeKey.stringKey("db.system.name"))
        );
        assertTrue(text.contains("?"), text);
        assertFalse(text.contains(QUERY_VALUE), text);
    }

    @Test
    void shouldMarkTheSpanOfAFailingQueryAsAnError() throws Exception {
        // given
        final var traceId = newTraceId();

        // when
        final var response = get(FAILING_QUERY_PATH, traceparent(traceId));

        // then
        assertEquals("failed", response.body());
        awaitServerSpanOfTrace(traceId);
        final var query = querySpanOfTrace(traceId);
        assertEquals("select no_such_table", query.getName());
        assertEquals(StatusCode.ERROR, query.getStatus().getStatusCode());
        assertFalse(
                query.getStatus().getDescription().contains("no_such_table"),
                query.getStatus().getDescription()
        );
        assertFalse(
                query.getEvents().toString().contains("no_such_table"),
                () -> query.getEvents().toString()
        );
    }

    @Test
    void shouldNotAddQuerySpans_toAManualScrape() throws Exception {
        // given
        final var traceId = newTraceId();

        // when
        final var response = getAsAdmin(SCRAPE_QUERY_PATH, traceparent(traceId));

        // then
        awaitServerSpanOfTrace(traceId);
        assertEquals(200, response.statusCode());
        assertEquals(1, spansOfTrace(traceId).size(), () -> "Spans: " + spansOfTrace(traceId));
    }

    @Test
    void shouldNotStartATrace_forAQueryOutsideOne() {
        // given
        final var marker = "untraced_" + newTraceId();

        // when
        dsl.fetchCount(DSL.table(DSL.name("public", "series_configuration")).as(marker));

        // then
        assertTrue(finishedSpans().stream().noneMatch(span -> String.valueOf(
                span.getAttributes().get(AttributeKey.stringKey("db.query.text"))
        ).contains(marker)));
    }

    private String traceparent(String traceId) {
        return "00-" + traceId + "-" + PARENT_SPAN_ID + "-01";
    }

    private String newTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private SpanData awaitServerSpanOfTrace(String traceId) throws InterruptedException {
        final var deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            final var match = spansOfTrace(traceId).stream()
                    .filter(span -> span.getKind() == SpanKind.SERVER)
                    .findFirst();
            if (match.isPresent()) {
                return match.get();
            }
            Thread.sleep(50);
        }
        throw new AssertionError("No span of trace " + traceId + ": " + finishedSpans());
    }

    private List<SpanData> spansOfTrace(String traceId) {
        return finishedSpans().stream().filter(span -> span.getTraceId().equals(traceId)).toList();
    }

    private SpanData querySpanOfTrace(String traceId) {
        return spansOfTrace(traceId).stream()
                .filter(span -> span.getKind() == SpanKind.CLIENT)
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "No query span of trace " + traceId + ": " + finishedSpans()
                ));
    }

    private List<SpanData> finishedSpans() {
        tracerProvider.forceFlush().join(10, TimeUnit.SECONDS);
        return spanExporter.getFinishedSpanItems();
    }

    private HttpResponse<String> getAsAdmin(String path, String traceparent) throws Exception {
        when(jwtDecoder.decode(ADMIN_TOKEN)).thenReturn(Jwt.withTokenValue(ADMIN_TOKEN)
                .header("alg", "ES256")
                .subject("admin")
                .claim("roles", List.of("ADMIN"))
                .build());
        final var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("traceparent", traceparent)
                .header("Authorization", "Bearer " + ADMIN_TOKEN)
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
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
        TracedRoute tracedRoute(DSLContext dsl) {
            return new TracedRoute(dsl);
        }

    }

    @RestController
    static class TracedRoute {

        private final DSLContext dsl;

        TracedRoute(DSLContext dsl) {
            this.dsl = dsl;
        }

        @GetMapping(QUERY_PATH)
        int query() {
            return countSeriesConfigurations();
        }

        @GetMapping(SCRAPE_QUERY_PATH)
        int queryDuringAManualScrape() {
            return countSeriesConfigurations();
        }

        @GetMapping(FAILING_QUERY_PATH)
        String failingQuery() {
            try {
                dsl.fetchCount(DSL.table(DSL.name("public", "no_such_table")));
                return "no failure";
            } catch (DataAccessException expected) {
                return "failed";
            }
        }

        private int countSeriesConfigurations() {
            return dsl.fetchCount(
                    DSL.table(DSL.name("public", "series_configuration")),
                    DSL.field(DSL.name("name"), String.class).eq(QUERY_VALUE)
            );
        }

        @GetMapping("/tracing-test")
        String traceIdInTheLoggingContext() {
            return MDC.get("traceId");
        }

    }

}
