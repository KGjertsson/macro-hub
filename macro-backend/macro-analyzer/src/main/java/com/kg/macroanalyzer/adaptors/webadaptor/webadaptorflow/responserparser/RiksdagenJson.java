package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser;

import com.kg.macroanalyzer.shared.web.ScrapeException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

// Riksdagen lists collapse to a single object for one hit and disappear entirely for none
final class RiksdagenJson {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private RiksdagenJson() {
    }

    static JsonNode readTree(String response, String source) throws ScrapeException {
        if (response == null || response.isBlank()) {
            throw new ScrapeException("Empty response from Riksdagen " + source);
        }
        try {
            return JSON_MAPPER.readTree(response);
        } catch (JacksonException e) {
            throw new ScrapeException("Failed to parse Riksdagen %s response: %s".formatted(source, e.getMessage()));
        }
    }

    static List<JsonNode> asList(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return List.of();
        }

        return node.isArray() ? List.copyOf(node.values()) : List.of(node);
    }

    static String absoluteUrl(String url) {
        return url.startsWith("//") ? "https:" + url : url;
    }

}
