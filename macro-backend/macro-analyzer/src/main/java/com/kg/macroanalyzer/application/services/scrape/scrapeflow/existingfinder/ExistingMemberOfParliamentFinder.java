package com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder;

import com.kg.macroanalyzer.application.ports.driven.ConfigWithMemberOfParliament;
import com.kg.macroanalyzer.scrape.application.service.scrapeflow.ExistingFinder;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;

import java.util.List;
import java.util.Optional;

// Riksdagen returns the full member list on every scrape and all members are upserted, so nothing is diffed here
public record ExistingMemberOfParliamentFinder() implements ExistingFinder<SeriesConfig, ConfigWithMemberOfParliament> {

    @Override
    public Optional<ConfigWithMemberOfParliament> findExisting(SeriesConfig seriesConfig) {
        return Optional.of(new ConfigWithMemberOfParliament(seriesConfig, List.of()));
    }

}
