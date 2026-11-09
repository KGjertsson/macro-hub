package com.kg.macroanalyzer.acceptance;

import tools.jackson.databind.json.JsonMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public record Mailbox(Gateway api, String smtpHost, int smtpPort) {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    public String latestTextTo(String address) {
        final var query = URLEncoder.encode("to:" + address, StandardCharsets.UTF_8);
        final var found = JSON.readTree(api.get("/api/v1/search?query=" + query).body()).get("messages");
        assertTrue(found != null && !found.isEmpty(), "No mail to " + address);

        final var message = api.get("/api/v1/message/" + found.get(0).get("ID").asString());
        assertEquals(200, message.statusCode());

        return JSON.readTree(message.body()).get("Text").asString();
    }

}
