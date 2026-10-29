package com.kg.macroanalyzer.adaptors.webadaptor;

import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.connectionbuilder.ScbConnectionBuilder;
import com.kg.macroanalyzer.adaptors.webadaptor.webadaptorflow.responserparser.ScbMetadataResponseParser;
import com.kg.macroanalyzer.application.domain.scb.ScbTableMetadata;
import com.kg.macroanalyzer.application.ports.driven.ScbMetadataRequest;
import com.kg.macroanalyzer.shared.web.HttpGetter;
import com.kg.macroanalyzer.shared.web.ScrapeException;
import com.kg.macroanalyzer.shared.web.WebAdaptorFlow;
import com.kg.macroanalyzer.shared.web.WebPort;

public class ScbMetadataWebAdaptor implements WebPort<ScbMetadataRequest, ScbTableMetadata> {

    private final ScbRetry retry;

    public ScbMetadataWebAdaptor(ScbRateLimiter rateLimiter) {
        this.retry = new ScbRetry(rateLimiter);
    }

    @Override
    public ScbTableMetadata fetch(ScbMetadataRequest request) throws ScrapeException {
        final var flow = new WebAdaptorFlow<>(
                new ScbConnectionBuilder(),
                new HttpGetter(),
                new ScbMetadataResponseParser(request.table())
        );

        return retry.fetch(flow, url(request));
    }

    static String url(ScbMetadataRequest request) {
        return "%s/tables/%s/metadata?lang=sv".formatted(request.baseUrl(), request.table());
    }

}
