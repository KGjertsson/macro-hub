package com.kg.macroanalyzer.adaptors.webadaptor;

import com.kg.macroanalyzer.application.ports.driven.ScbDataRequest;
import com.kg.macroanalyzer.application.ports.driven.ScbMetadataRequest;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScbDataWebAdaptorTest {

    private static final String BASE_URL = "https://statistikdatabasen.scb.se/api/v2";

    @Test
    void shouldEncodeCodesAndWildcard_butKeepCommasBetweenCodes() {
        // given
        final var selection = new LinkedHashMap<String, List<String>>();
        selection.put("ContentsCode", List.of("000007DQ"));
        selection.put("Upplatelseform", List.of("ÄG/ANDEL", "TOTALT"));
        selection.put("Kon", List.of("1+2"));
        final var request = new ScbDataRequest(BASE_URL, "TAB6253", "????????", List.of("2022", "2023"), selection);

        // when
        final var url = ScbDataWebAdaptor.url(request);

        // then
        assertEquals(BASE_URL + "/tables/TAB6253/data?lang=sv&outputFormat=json-stat2"
                + "&valueCodes%5BRegion%5D=%3F%3F%3F%3F%3F%3F%3F%3F"
                + "&valueCodes%5BTid%5D=2022,2023"
                + "&valueCodes%5BContentsCode%5D=000007DQ"
                + "&valueCodes%5BUpplatelseform%5D=%C3%84G%2FANDEL,TOTALT"
                + "&valueCodes%5BKon%5D=1%2B2", url);
    }

    @Test
    void shouldBuildTheMetadataUrl() {
        assertEquals(
                BASE_URL + "/tables/TAB6574/metadata?lang=sv",
                ScbMetadataWebAdaptor.url(new ScbMetadataRequest(BASE_URL, "TAB6574"))
        );
    }

}
