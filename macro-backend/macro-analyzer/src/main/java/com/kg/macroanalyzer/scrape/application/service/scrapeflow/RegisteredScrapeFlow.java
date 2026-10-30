package com.kg.macroanalyzer.scrape.application.service.scrapeflow;

import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem;
import com.kg.macroanalyzer.scrape.domain.ScrapeResult;

// A domain's scrape flow registered under a flow id. ScrapeService routes a queue item to the flow named by its
// series config's scrapeFlow; the ids are owned by the modules and opaque to scrape
public interface RegisteredScrapeFlow {

    String flowId();

    ScrapeResult run(ScrapeQueueItem item);

    static RegisteredScrapeFlow of(String flowId, ScrapeFlow<ScrapeQueueItem, ?, ?> flow) {
        return new Of(flowId, flow);
    }

    record Of(String flowId, ScrapeFlow<ScrapeQueueItem, ?, ?> flow) implements RegisteredScrapeFlow {

        @Override
        public ScrapeResult run(ScrapeQueueItem item) {
            return flow.run(item);
        }

    }

}
