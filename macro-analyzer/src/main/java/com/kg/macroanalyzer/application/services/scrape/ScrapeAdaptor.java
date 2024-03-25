package com.kg.macroanalyzer.application.services.scrape;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import com.kg.macroanalyzer.utils.WebUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class ScrapeAdaptor {

    public List<MacroPoint> scrapeNovelItems(
            SeriesConfig seriesConfig,
            List<MacroPoint> persistedItems
    ) throws ScrapeException {
        try {
            final var response = WebUtils.getHTTP(seriesConfig.scrapeUrl());
            final var objectMapper = createObjectMapper();

            JavaType collectionType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, ScrapedResponse.class);
            final List<ScrapedResponse> resultList = objectMapper.readValue(response, collectionType);

            return resultList.stream()
                    .map(ScrapedResponse::toMacroPoint)
                    .filter(i -> !persistedItems.contains(i))
                    .toList();
        } catch (IOException ioException) {
            final var msgRaw = "Encountered IOException when scraping %s, e=%s";
            final var msgFormatted = msgRaw.formatted(seriesConfig.displayName(), ioException);
            log.error(msgFormatted);

            throw new ScrapeException(msgFormatted);
        }
    }

    private ObjectMapper createObjectMapper() {
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
