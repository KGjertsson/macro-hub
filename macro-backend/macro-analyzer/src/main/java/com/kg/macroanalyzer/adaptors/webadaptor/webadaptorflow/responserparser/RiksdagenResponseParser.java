package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kg.macroanalyzer.application.domain.parliament.MemberOfParliament;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public record RiksdagenResponseParser() implements ResponseParser<String, Stream<MemberOfParliament>> {

    @Override
    public Stream<MemberOfParliament> parse(String response) throws ScrapeException {
        try {
            final var objectMapper = new ObjectMapper();
            objectMapper.coercionConfigFor(com.fasterxml.jackson.databind.type.LogicalType.POJO)
                    .setCoercion(
                            com.fasterxml.jackson.databind.cfg.CoercionInputShape.EmptyString,
                            com.fasterxml.jackson.databind.cfg.CoercionAction.AsNull
                    );
            final var riksdagenResponse = objectMapper.readValue(response, RiksdagenResponse.class);

            if (riksdagenResponse.personlista() == null || riksdagenResponse.personlista().person() == null) {
                return Stream.empty();
            }

            return riksdagenResponse.personlista().person().stream();
        } catch (IOException e) {
            log.error("Failed to parse Riksdagen response", e);
            throw new ScrapeException("Failed to parse Riksdagen response: " + e.getMessage());
        }
    }

    private record RiksdagenResponse(PersonLista personlista) {
    }

    private record PersonLista(
            @JsonProperty("@systemdatum") String systemDatum,
            @JsonProperty("@hits") String hits,
            List<MemberOfParliament> person
    ) {
    }

}
