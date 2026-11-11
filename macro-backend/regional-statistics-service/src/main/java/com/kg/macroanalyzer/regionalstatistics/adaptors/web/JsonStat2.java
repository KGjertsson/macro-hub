package com.kg.macroanalyzer.regionalstatistics.adaptors.web;

import com.kg.macroanalyzer.shared.web.ScrapeException;
import lombok.experimental.UtilityClass;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// category.index is either an array of codes or an object from code to position
@UtilityClass
class JsonStat2 {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

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
            throw new ScrapeException(
                    "Failed to parse SCB response for %s: %s".formatted(source, e.getMessage())
            );
        }
    }

    static Map<String, List<String>> codes(JsonNode root) {
        final var codes = new LinkedHashMap<String, List<String>>();
        root.path("id").valueStream()
                .map(JsonNode::asString)
                .forEach(dimension -> codes.put(
                        dimension,
                        dimensionCodes(root.path("dimension").path(dimension))
                ));

        return Collections.unmodifiableMap(codes);
    }

    private static List<String> dimensionCodes(JsonNode dimension) {
        final var index = dimension.path("category").path("index");
        if (index.isArray()) {
            return index.valueStream().map(JsonNode::asString).toList();
        }
        final var ordered = new String[index.size()];
        index.properties().forEach(e -> ordered[e.getValue().asInt()] = e.getKey());

        return Collections.unmodifiableList(Arrays.asList(ordered));
    }

}
