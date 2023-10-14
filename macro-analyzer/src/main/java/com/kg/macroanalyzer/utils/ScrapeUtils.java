package com.kg.macroanalyzer.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.List;

public class ScrapeUtils {

    public static <E> List<E> scrapeNovelItems(
            String url,
            List<E> persistedItems,
            Class<E> itemType
    ) throws IOException {
        final var response = WebUtils.getHTTP(url);
        final var objectMapper = ScrapeUtils.createObjectMapper();

        JavaType collectionType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, itemType);
        List<E> resultList = objectMapper.readValue(response, collectionType);

        return resultList.stream().filter(i -> !persistedItems.contains(i)).toList();
    }

    private static ObjectMapper createObjectMapper() {
        final var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

}
