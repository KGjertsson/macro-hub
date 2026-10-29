package com.kg.macroanalyzer.adaptors.webadaptor;

import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.WebAdaptorFlow;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.connectionbuilder.ScbConnectionBuilder;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.getter.HttpGetter;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.ScbDataResponseParser;
import com.kg.macroanalyzer.application.domain.scb.ScbTableMetadata;
import com.kg.macroanalyzer.application.domain.scb.ScbTableValues;
import com.kg.macroanalyzer.application.exceptions.ScrapeException;
import com.kg.macroanalyzer.application.ports.driven.ScbDataRequest;
import com.kg.macroanalyzer.application.ports.driven.WebPort;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class ScbDataWebAdaptor implements WebPort<ScbDataRequest, ScbTableValues> {

    private final ScbRateLimiter rateLimiter;

    public ScbDataWebAdaptor(ScbRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public ScbTableValues fetch(ScbDataRequest request) throws ScrapeException {
        final var flow = new WebAdaptorFlow<>(
                new ScbConnectionBuilder(),
                new HttpGetter(),
                new ScbDataResponseParser(request.table())
        );

        return ScbRetry.fetch(flow, url(request), rateLimiter);
    }

    // Codes such as "1+2", "ÄG/ANDEL" and the "?" wildcard must be encoded; the commas between codes must not
    static String url(ScbDataRequest request) {
        final var url = new StringBuilder("%s/tables/%s/data?lang=sv&outputFormat=json-stat2".formatted(
                request.baseUrl(), request.table()));
        appendValueCodes(url, ScbTableMetadata.REGION, List.of(request.regionCodes()));
        appendValueCodes(url, ScbTableMetadata.TIME, request.years());
        request.selection().forEach((dimension, codes) -> appendValueCodes(url, dimension, codes));

        return url.toString();
    }

    private static void appendValueCodes(StringBuilder url, String dimension, List<String> codes) {
        url.append("&").append(encode("valueCodes[" + dimension + "]")).append("=")
                .append(codes.stream().map(ScbDataWebAdaptor::encode).collect(Collectors.joining(",")));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

}
