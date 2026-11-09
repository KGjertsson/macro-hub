package com.kg.macroanalyzer.shared.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.regex.Pattern;

// Starts the span directly: an Observation would also add a timer per query name to the metrics
public final class QuerySpanListener implements ExecuteListener {

    private static final String SPAN = QuerySpanListener.class.getName() + ".span";
    private static final String DEFAULT_NAME = "query";
    private static final String SCRAPE_PATH = "/macro-analyzer/scrape/";
    private static final Pattern OPERATION = Pattern.compile("^\\s*(\\w+)");
    // The lookahead skips a column, as in extract(year from "public"."t"."date")
    private static final Pattern TABLE = Pattern.compile("\\b(?:from|into|update)\\s+(?:\"[^\"]+\"\\.)?\"([^\"]+)\"(?!\\.)", Pattern.CASE_INSENSITIVE);

    private final Tracer tracer;

    public QuerySpanListener(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void start(ExecuteContext context) {
        // Without a parent a span starts a new trace, which every query of a cron run would then be
        final var parent = tracer.currentSpan();
        if (parent != null && !isManualScrape()) {
            context.data(SPAN, tracer.spanBuilder().setParent(parent.context()).kind(Span.Kind.CLIENT).start());
        }
    }

    @Override
    public void end(ExecuteContext context) {
        if (!(context.data(SPAN) instanceof Span span)) {
            return;
        }
        final var sql = context.sql();
        span.name(spanName(sql)).tag("db.system.name", "postgresql");
        if (sql != null) {
            span.tag("db.query.text", sql);
        }
        if (context.exception() != null) {
            span.error(new QueryFailed(context.exception().getClass().getName()));
        }
        span.end();
    }

    // A scrape can run a very large number of statements under its one request, more than a trace can usefully show
    private boolean isManualScrape() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes
                && attributes.getRequest().getRequestURI().startsWith(SCRAPE_PATH);
    }

    private static String spanName(String sql) {
        if (sql == null) {
            return DEFAULT_NAME;
        }
        final var operation = OPERATION.matcher(sql);
        if (!operation.find()) {
            return DEFAULT_NAME;
        }
        final var table = TABLE.matcher(sql);
        return table.find() ? operation.group(1).toLowerCase() + " " + table.group(1) : operation.group(1).toLowerCase();
    }

    // In place of the real exception, whose message can repeat a bound value: Postgres's "Key (email)=(...) already exists"
    private static final class QueryFailed extends RuntimeException {

        private QueryFailed(String exceptionType) {
            super(exceptionType, null, false, false);
        }

    }

}
