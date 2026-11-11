package com.kg.macroanalyzer.macroseries.adaptors.http;

import com.kg.macroanalyzer.macroseries.application.port.in.MacroSeriesQueries;
import com.kg.macroanalyzer.macroseries.domain.AlignedBundle;
import com.kg.macroanalyzer.macroseries.domain.MacroPoint;
import com.kg.macroanalyzer.macroseries.domain.MacroSeries;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Goes through the real HTTP layer, to pin what the frontend relies on: the wire format of the
 * MVC message converter and the CORS headers. The controller's own logic is in
 * MacroSeriesControllerTest.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class MacroSeriesControllerHttpTest {

    private static final String CHART_DATA_REQUEST = """
            {"strategy":"MONTH","timeFrame":"ALL","chartSeriesParams":[{"name":"seriesA"}]}""";
    private static final String ORIGIN = "http://example.test";

    @Value("${local.server.port}")
    int port;

    @MockitoBean
    MacroSeriesQueries macroSeriesQueries;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void shouldWriteMonthLabelsAsArrays_andKeepThePropertyOrder() throws Exception {
        // given
        givenChartDataWithLabels(YearMonth.of(1994, 6), YearMonth.of(1994, 7));

        // when
        final var response = postChartData();

        // then
        assertEquals(200, response.statusCode());
        assertEquals(
                """
                {"labels":[[1994,6],[1994,7]],"chartData":[{"values":[1.0,2.0],\
                "name":"seriesA","color":"rgb(207,82,48)"}]}""",
                response.body()
        );
    }

    @Test
    void shouldWriteDayLabelsAsArrays() throws Exception {
        // given
        givenChartDataWithLabels(LocalDate.of(1994, 6, 1), LocalDate.of(1994, 6, 2));

        // when
        final var response = postChartData();

        // then
        assertEquals(200, response.statusCode());
        assertEquals(
                """
                {"labels":[[1994,6,1],[1994,6,2]],"chartData":[{"values":[1.0,2.0],\
                "name":"seriesA","color":"rgb(207,82,48)"}]}""",
                response.body()
        );
    }

    @Test
    void shouldAllowAnyOrigin_onChartDataPost() throws Exception {
        // given
        givenChartDataWithLabels(YearMonth.of(1994, 6), YearMonth.of(1994, 7));

        // when
        final var response = postChartData();

        // then
        assertEquals(200, response.statusCode());
        assertEquals(
                Optional.of("*"),
                response.headers().firstValue("Access-Control-Allow-Origin")
        );
    }

    @Test
    void shouldAnswerPreflight_forChartDataPost() throws Exception {
        // given
        final var request = HttpRequest.newBuilder(chartDataUri())
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .header("Origin", ORIGIN)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "content-type")
                .build();

        // when
        final var response = send(request);

        // then
        final var headers = response.headers();
        assertEquals(200, response.statusCode());
        assertEquals(Optional.of("*"), headers.firstValue("Access-Control-Allow-Origin"));
        assertEquals(
                Optional.of("GET,POST,PUT,DELETE"),
                headers.firstValue("Access-Control-Allow-Methods")
        );
        assertEquals(
                Optional.of("content-type"),
                headers.firstValue("Access-Control-Allow-Headers")
        );
    }

    private void givenChartDataWithLabels(Temporal first, Temporal second) {
        final var series = new MacroSeries(
                "seriesA",
                List.of(new MacroPoint(1.0, first), new MacroPoint(2.0, second))
        );
        final var bundle = AlignedBundle.builder()
                .labels(List.of(first, second))
                .macroSeries(List.of(series))
                .build();
        when(macroSeriesQueries.buildAlignedBundle(any())).thenReturn(Optional.of(bundle));
    }

    private HttpResponse<String> postChartData() throws IOException, InterruptedException {
        return send(HttpRequest.newBuilder(chartDataUri())
                .POST(HttpRequest.BodyPublishers.ofString(CHART_DATA_REQUEST))
                .header("Content-Type", "application/json")
                .header("Origin", ORIGIN)
                .build());
    }

    private HttpResponse<String> send(HttpRequest request)
            throws IOException, InterruptedException {
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI chartDataUri() {
        return URI.create("http://localhost:" + port + "/macro-analyzer/chart-data");
    }

}
