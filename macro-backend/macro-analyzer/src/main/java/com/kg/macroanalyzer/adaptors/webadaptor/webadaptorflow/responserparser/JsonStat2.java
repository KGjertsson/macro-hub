package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser;

import com.kg.macroanalyzer.shared.web.ScrapeException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Reads the dimensions of a JSON-stat 2.0 dataset, which SCB's PxWeb API returns for both metadata and data.
// category.index is either an array of codes or an object from code to position
final class JsonStat2 {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private JsonStat2() {
    }

    static JsonNode readTree(String response, String source) throws ScrapeException {
        if (response == null || response.isBlank()) {
            throw new ScrapeException("Empty response from SCB for " + source);
        }
        try {
            final var root = JSON_MAPPER.readTree(response);
            if (!root.path("id").isArray() || !root.path("dimension").isObject()) {
                throw new ScrapeException("Not a JSON-stat2 dataset from SCB for " + source);
            }

            return root;
        } catch (JacksonException e) {
            throw new ScrapeException("Failed to parse SCB response for %s: %s".formatted(source, e.getMessage()));
        }
    }

    // Codes per dimension, in the dataset's dimension order and each dimension's code order
    static Map<String, List<String>> codes(JsonNode root) {
        final var codes = new LinkedHashMap<String, List<String>>();
        root.path("id").valueStream()
                .map(JsonNode::asString)
                .forEach(dimension -> codes.put(dimension, dimensionCodes(root.path("dimension").path(dimension))));

        return codes;
    }

    private static List<String> dimensionCodes(JsonNode dimension) {
        final var index = dimension.path("category").path("index");
        if (index.isArray()) {
            return index.valueStream().map(JsonNode::asString).toList();
        }
        final var ordered = new String[index.size()];
        index.properties().forEach(e -> ordered[e.getValue().asInt()] = e.getKey());

        return new ArrayList<>(Arrays.asList(ordered));
    }

}
