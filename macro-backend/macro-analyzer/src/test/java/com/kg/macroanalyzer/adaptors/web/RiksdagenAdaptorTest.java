package com.kg.macroanalyzer.adaptors.web;

import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RiksdagenAdaptorTest {

    @Test
    void testParseResponseWithEmptyStrings() throws ScrapeException {
        String json = """
                {
                  "personlista": {
                    "person": [
                      {
                        "hangar_guid": "guid",
                        "personuppdrag": "",
                        "personuppgift": ""
                      }
                    ]
                  }
                }
                """;

        RiksdagenAdaptor adaptor = new RiksdagenAdaptor();

        var result = adaptor.parseResponse(json).toList();
        assertNotNull(result);
    }

    @Test
    void testParseResponseWithNormalData() throws ScrapeException {
        String json = """
                {
                  "personlista": {
                    "person": [
                      {
                        "hangar_guid": "guid",
                        "personuppdrag": {
                            "uppdrag": [
                                {
                                    "organ_kod": "UTU"
                                }
                            ]
                        },
                        "personuppgift": {
                            "uppgift": [
                                {
                                    "kod": "uppdrag"
                                }
                            ]
                        }
                      }
                    ]
                  }
                }
                """;

        RiksdagenAdaptor adaptor = new RiksdagenAdaptor();

        var result = adaptor.parseResponse(json).toList();
        assertNotNull(result);
    }
}
