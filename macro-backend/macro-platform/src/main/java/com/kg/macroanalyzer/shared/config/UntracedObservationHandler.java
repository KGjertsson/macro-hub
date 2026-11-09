package com.kg.macroanalyzer.shared.config;

import io.micrometer.observation.Observation;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.TracingObservationHandler;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.scheduling.support.ScheduledTaskObservationContext;

// Prometheus scrapes /actuator/prometheus every 15 seconds and every @Scheduled run starts an observation, so neither may
// be a span. An ObservationPredicate would also stop their metrics. Nothing may simply skip the tracing handlers either:
// TracingAwareMeterObservationHandler requires the context this handler leaves in place, or stopping the observation throws
public final class UntracedObservationHandler implements TracingObservationHandler<Observation.Context> {

    private static final String ACTUATOR_PATH = "/actuator";

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof ScheduledTaskObservationContext || isActuatorRequest(context);
    }

    @Override
    public void onStart(Observation.Context context) {
        getTracingContext(context);
    }

    @Override
    public void onEvent(Observation.Event event, Observation.Context context) {
    }

    @Override
    public void onError(Observation.Context context) {
    }

    @Override
    public Tracer getTracer() {
        return Tracer.NOOP;
    }

    private static boolean isActuatorRequest(Observation.Context context) {
        if (context instanceof ServerRequestObservationContext request && request.getCarrier() != null) {
            final var path = request.getCarrier().getRequestURI();
            return path.equals(ACTUATOR_PATH) || path.startsWith(ACTUATOR_PATH + "/");
        }
        return false;
    }

}
