package com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kg.macroanalyzer.application.domain.parliament.MemberOfParliament;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.LogicalType;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
public record RiksdagenResponseParser() implements ResponseParser<String, Stream<MemberOfParliament>> {

    @Override
    public Stream<MemberOfParliament> parse(String response) throws ScrapeException {
        try {
            final var objectMapper = JsonMapper.builder()
                    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .withCoercionConfig(LogicalType.POJO, config -> config.setCoercion(
                            CoercionInputShape.EmptyString,
                            CoercionAction.AsNull
                    ))
                    .build();
            final var riksdagenResponse = objectMapper.readValue(response, RiksdagenResponse.class);

            if (riksdagenResponse.personlista() == null || riksdagenResponse.personlista().person() == null) {
                return Stream.empty();
            }

            return riksdagenResponse.personlista().person().stream();
        } catch (JacksonException e) {
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
