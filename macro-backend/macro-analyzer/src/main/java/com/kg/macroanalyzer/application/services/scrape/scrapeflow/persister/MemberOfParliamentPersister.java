package com.kg.macroanalyzer.application.services.scrape.scrapeflow.persister;

import com.kg.macroanalyzer.application.ports.driven.ConfigWithMemberOfParliament;
import com.kg.macroanalyzer.application.services.scrape.ScrapeResult;

public record MemberOfParliamentPersister() implements Persister<ConfigWithMemberOfParliament> {

    @Override
    public ScrapeResult persist(ConfigWithMemberOfParliament scraped) {
        return null;
    }

}
