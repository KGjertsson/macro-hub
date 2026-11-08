package com.kg.macroanalyzer.scrape.application.port.out;

import com.kg.macroanalyzer.scrape.domain.ScrapeResult;

import java.time.Duration;
import java.util.Collection;

public interface ScrapeMetricsPort {

    String UNKNOWN_FLOW = "unknown";

    void registerFlows(Collection<String> flowIds);

    void recordItem(String flowId, ScrapeResult result, Duration duration);

}
