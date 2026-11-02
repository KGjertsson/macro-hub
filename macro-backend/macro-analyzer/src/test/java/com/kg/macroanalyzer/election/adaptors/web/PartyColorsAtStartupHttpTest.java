package com.kg.macroanalyzer.election.adaptors.web;

import com.kg.macroanalyzer.election.application.port.out.PartyColorPort;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import static org.awaitility.Awaitility.await;

/** The wiring: the real application, with its default intervals, loads the list soon after it has started. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PartyColorsAtStartupHttpTest {

    static HttpServer stub;

    @Autowired
    PartyColorPort partyColorPort;

    @DynamicPropertySource
    static void pointAtStub(DynamicPropertyRegistry registry) throws IOException {
        stub = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        stub.createContext("/macro-analyzer/parliament/parties", exchange -> {
            final var body = """
                    [{"code":"SD","color":"#FFCC00"}]
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        stub.start();
        registry.add("election.parliament.url", () -> "http://localhost:" + stub.getAddress().getPort());
    }

    @AfterAll
    static void stopStub() {
        stub.stop(0);
    }

    @Test
    void shouldLoadTheColors_soonAfterStart() {
        await().atMost(Duration.ofSeconds(10)).until(() -> Map.of("SD", "#FFCC00").equals(partyColorPort.partyColors()));
    }

}
