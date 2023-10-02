package com.kg.macroanalyzer.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class ScrapeUtils {

    ObjectMapper objectMapper;

    @Autowired
    public ScrapeUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <E> List<E> scrapeNovelItems(String url, List<E> persistedItems, Class<E> itemType) throws IOException {
        final var response = WebUtils.getHTTP(url);
        JavaType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, itemType);
        List<E> resultList = objectMapper.readValue(response, collectionType);

        return resultList.stream().filter(i -> !persistedItems.contains(i)).toList();
    }


}
