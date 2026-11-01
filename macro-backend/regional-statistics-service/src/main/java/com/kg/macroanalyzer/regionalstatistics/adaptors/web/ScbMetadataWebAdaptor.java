package com.kg.macroanalyzer.regionalstatistics.adaptors.web;

import com.kg.macroanalyzer.regionalstatistics.application.port.out.ScbMetadataRequest;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbTableMetadata;
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
