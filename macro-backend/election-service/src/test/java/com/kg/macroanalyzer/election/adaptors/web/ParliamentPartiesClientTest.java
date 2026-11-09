package com.kg.macroanalyzer.election.adaptors.web;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;

class ParliamentPartiesClientTest {

    private static final String PATH = "/macro-analyzer/parliament/parties";
    private static final Duration SHORT_TIMEOUT = Duration.ofMillis(300);

    HttpServer server;
    // Released at the end of a test, so a stub that hangs lets the server stop
    final CountDownLatch release = new CountDownLatch(1);

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    @AfterEach
    void stopServer() {
        release.countDown();
        server.stop(0);
    }

    // The consumer's half of the contract: PartiesContractHttpTest holds parliament to the same file
    @Test
    void shouldReadColors_fromTheContractFile() throws Exception {
        // given
        try (final var contract = getClass().getResourceAsStream("/contracts/parliament-parties.json")) {
            stub(200, new String(contract.readAllBytes(), StandardCharsets.UTF_8));
        }

        // when
        try (final var client = client(Duration.ofSeconds(5))) {
            // then
            assertEquals(Map.of("S", "#E8112d", "SD", "#FFCC00"), client.fetchPartyColors());
        }
    }

    @Test
    void shouldIgnoreFieldsItDoesNotRead_andAcceptATrailingSlashInTheUrl() throws Exception {
        // given
        stub(200, """
                [{"code":"S","name":"Socialdemokraterna","color":"#E8112d","ideology":"Social Democracy","active":true}]
                """);

        // when
        try (final var client = new ParliamentPartiesClient(RestClient.builder(), url() + "/", Duration.ofSeconds(5), new JsonMapper())) {
            // then
            assertEquals(Map.of("S", "#E8112d"), client.fetchPartyColors());
        }
    }

    @Test
    void shouldReturnNoColors_onNoContent() throws Exception {
        // given
        server.createContext(PATH, exchange -> {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });

        // when
        try (final var client = client(Duration.ofSeconds(5))) {
            // then
            assertEquals(Map.of(), client.fetchPartyColors());
        }
    }

    @Test
    void shouldFail_onAnErrorStatus() {
        // given
        stub(500, "[]");

        // then
        assertFails(IOException.class, Duration.ofSeconds(5));
    }

    @Test
    void shouldFail_whenTheAnswerIsNotAListOfParties() {
        // given
        stub(200, "<html>the frontend's index.html</html>");

        // then
        assertFails(IOException.class, Duration.ofSeconds(5));
    }

    @Test
    void shouldFail_whenThePartiesAreJsonNull() {
        // given
        stub(200, "null");

        // then
        assertFails(IOException.class, Duration.ofSeconds(5));
    }

    @Test
    void shouldFail_whenAFieldItReadsIsMissing() {
        // given
        stub(200, """
                [{"code":"S","colour":"#E8112d"}]
                """);

        // then
        assertFails(IOException.class, Duration.ofSeconds(5));
    }

    @Test
    void shouldFail_whenNothingListens() {
        // given
        final var url = url();
        server.stop(0);

        // when
        try (final var client = new ParliamentPartiesClient(RestClient.builder(), url, Duration.ofSeconds(5), new JsonMapper())) {
            // then
            assertThrows(IOException.class, client::fetchPartyColors);
        }
    }

    @Test
    void shouldTimeOut_whenTheServerNeverAnswers() {
        // given
        server.createContext(PATH, exchange -> awaitRelease());

        // then
        assertTimeout(Duration.ofSeconds(3), () -> assertFails(TimeoutException.class, SHORT_TIMEOUT));
    }

    @Test
    void shouldTimeOut_whenTheBodyStallsAfterTheHeaders() {
        // given
        server.createContext(PATH, exchange -> {
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write("[".getBytes(StandardCharsets.UTF_8));
            exchange.getResponseBody().flush();
            awaitRelease();
        });

        // then
        assertTimeout(Duration.ofSeconds(3), () -> assertFails(TimeoutException.class, SHORT_TIMEOUT));
    }

    private void assertFails(Class<? extends Exception> expected, Duration timeout) {
        try (final var client = client(timeout)) {
            assertThrows(expected, client::fetchPartyColors);
        }
    }

    private void stub(int status, String body) {
        server.createContext(PATH, exchange -> {
            final var bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
    }

    private void awaitRelease() {
        try {
            release.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private ParliamentPartiesClient client(Duration timeout) {
        return new ParliamentPartiesClient(RestClient.builder(), url(), timeout, new JsonMapper());
    }

    private String url() {
        return "http://localhost:" + server.getAddress().getPort();
    }

}
