package com.kg.macroanalyzer.regionalstatistics.adaptors.config;

import com.kg.macroanalyzer.regionalstatistics.adaptors.web.ScbDataWebAdaptor;
import com.kg.macroanalyzer.regionalstatistics.adaptors.web.ScbMetadataWebAdaptor;
import com.kg.macroanalyzer.regionalstatistics.adaptors.web.ScbRateLimiter;
import com.kg.macroanalyzer.regionalstatistics.application.port.out.ScbDataRequest;
import com.kg.macroanalyzer.regionalstatistics.application.port.out.ScbMetadataRequest;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbTableMetadata;
import com.kg.macroanalyzer.regionalstatistics.domain.ScbTableValues;
import com.kg.macroanalyzer.shared.web.WebPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScbWebAdaptorConfiguration {

    @Bean("scbRateLimiter")
    public ScbRateLimiter scbRateLimiter(@Value("${scb.delay-ms:350}") long delayMs) {
        return new ScbRateLimiter(delayMs);
    }

    @Bean("scbMetadataWebAdaptor")
    public WebPort<ScbMetadataRequest, ScbTableMetadata> scbMetadataWebAdaptor(ScbRateLimiter scbRateLimiter) {
        return new ScbMetadataWebAdaptor(scbRateLimiter);
    }

    @Bean("scbDataWebAdaptor")
    public WebPort<ScbDataRequest, ScbTableValues> scbDataWebAdaptor(ScbRateLimiter scbRateLimiter) {
        return new ScbDataWebAdaptor(scbRateLimiter);
    }

}
