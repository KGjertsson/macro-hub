package com.kg.macroanalyzer.adaptors.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kg.macroanalyzer.application.domain.parliament.MemberOfParliament;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
public class RiksdagenAdaptor extends WebAdaptor<MemberOfParliament> {

    @Override
    protected HttpURLConnection buildConnection(SeriesConfig seriesConfig) throws IOException, URISyntaxException {
        final var url = new URI(seriesConfig.scrapeUrl()).toURL();
        final var connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8");
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.7");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Referer", "https://data.riksdagen.se/personlista/");
        connection.setRequestProperty("Sec-Fetch-Dest", "document");
        connection.setRequestProperty("Sec-Fetch-Mode", "navigate");
        connection.setRequestProperty("Sec-Fetch-Site", "same-origin");
        connection.setRequestProperty("Sec-Fetch-User", "?1");
        connection.setRequestProperty("Sec-GPC", "1");
        connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36");
        connection.setRequestProperty("sec-ch-ua", "\"Chromium\";v=\"142\", \"Brave\";v=\"142\", \"Not_A Brand\";v=\"99\"");
        connection.setRequestProperty("sec-ch-ua-mobile", "?0");
        connection.setRequestProperty("sec-ch-ua-platform", "\"macOS\"");

        return connection;
    }

    @Override
    protected Stream<MemberOfParliament> parseResponse(String response) throws ScrapeException {
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
