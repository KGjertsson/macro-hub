package com.kg.macroanalyzer.scrape.application.service.scrapeflow;

import com.kg.macroanalyzer.scrape.application.service.ScrapeResult;
import com.kg.macroanalyzer.scrape.domain.ScrapeQueueItem;
import com.kg.macroanalyzer.scrape.domain.ScrapeQueueKind;

// A domain's scrape flow registered for one queue kind; ScrapeService routes queue items by kind
public interface KindedScrapeFlow {

    ScrapeQueueKind kind();

    ScrapeResult run(ScrapeQueueItem item);

    static KindedScrapeFlow of(ScrapeQueueKind kind, ScrapeFlow<ScrapeQueueItem, ?, ?> flow) {
        return new Of(kind, flow);
    }

    record Of(ScrapeQueueKind kind, ScrapeFlow<ScrapeQueueItem, ?, ?> flow) implements KindedScrapeFlow {

        @Override
        public ScrapeResult run(ScrapeQueueItem item) {
            return flow.run(item);
        }

    }

}
