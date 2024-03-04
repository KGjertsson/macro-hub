package com.kg.macroanalyzer.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import lombok.NonNull;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ScrapeUtils {

    public static List<MacroPoint> scrapeNovelItems(
            SeriesConfig seriesConfig,
            List<MacroPoint> persistedItems
    ) throws IOException {
        final var response = WebUtils.getHTTP(seriesConfig.scrapeUrl());
        final var objectMapper = ScrapeUtils.createObjectMapper();

        JavaType collectionType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, ScrapedResponse.class);
        final List<ScrapedResponse> resultList = objectMapper.readValue(response, collectionType);

        return resultList.stream()
                .map(ScrapedResponse::toMacroPoint)
                .filter(i -> !persistedItems.contains(i))
                .toList();
    }

    private static ObjectMapper createObjectMapper() {
        final var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    private record ScrapedResponse(@NonNull Double value, @NonNull String date) {

        public MacroPoint toMacroPoint() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            return MacroPoint.builder()
                    .value(value)
                    .date(LocalDate.parse(date, formatter))
                    .build();
        }

    }

}
