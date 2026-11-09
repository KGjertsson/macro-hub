package com.kg.macroanalyzer.shared.config;

import io.micrometer.tracing.Tracer;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.boot.micrometer.tracing.autoconfigure.MicrometerTracingAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class TracingConfiguration {

    @Bean
    @Order(MicrometerTracingAutoConfiguration.RECEIVER_TRACING_OBSERVATION_HANDLER_ORDER - 1)
    public UntracedObservationHandler untracedObservationHandler() {
        return new UntracedObservationHandler();
    }

    @Bean
    public DefaultExecuteListenerProvider querySpanListenerProvider(Tracer tracer) {
        return new DefaultExecuteListenerProvider(new QuerySpanListener(tracer));
    }

}
