package com.kg.macroanalyzer.adaptors.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kg.macroanalyzer.application.domain.MacroPoint;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
public class RiksdagenAdaptor extends WebAdaptor {

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
    protected Stream<MacroPoint> parseResponse(String response) throws ScrapeException {
        try {
            final var objectMapper = new ObjectMapper();
            final var riksdagenResponse = objectMapper.readValue(response, RiksdagenResponse.class);

            if (riksdagenResponse.personlista() == null || riksdagenResponse.personlista().person() == null) {
                return Stream.empty();
            }

            return riksdagenResponse.personlista().person().stream()
                    .map(person -> MacroPoint.builder()
                            .value(0.0) // Placeholder
                            .date(LocalDate.now()) // Placeholder
                            .build());
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
            List<Person> person
    ) {
    }

    private record Person(
            String hangar_guid,
            String sourceid,
            String intressent_id,
            String hangar_id,
            String fodd_ar,
            String kon,
            String efternamn,
            String tilltalsnamn,
            String sorteringsnamn,
            String iort,
            String parti,
            String valkrets,
            String status,
            String person_url_xml,
            String bild_url_80,
            String bild_url_192,
            String bild_url_max,
            PersonUppdrag personuppdrag,
            PersonUppgift personuppgift
    ) {
    }

    private record PersonUppdrag(List<Uppdrag> uppdrag) {
    }

    private record Uppdrag(
            String organ_kod,
            String roll_kod,
            String ordningsnummer,
            String status,
            String typ,
            String from,
            String tom,
            List<Object> uppgift,
            String intressent_id,
            String hangar_id,
            String sortering,
            String organ_sortering,
            String uppdrag_rollsortering,
            String uppdrag_statussortering
    ) {
    }

    private record PersonUppgift(List<Uppgift> uppgift) {
    }

    private record Uppgift(
            String kod,
            List<Object> uppgift,
            String typ,
            String intressent_id,
            String hangar_id
    ) {
    }

}
