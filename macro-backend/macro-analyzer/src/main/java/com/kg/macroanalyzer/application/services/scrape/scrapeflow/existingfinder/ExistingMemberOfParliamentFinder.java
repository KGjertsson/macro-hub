package com.kg.macroanalyzer.application.services.scrape.scrapeflow.existingfinder;

import com.kg.macroanalyzer.application.ports.driven.ConfigWithMemberOfParliament;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;

import java.util.Optional;

public record ExistingMemberOfParliamentFinder(

) implements ExistingFinder<SeriesConfig, ConfigWithMemberOfParliament> {

    @Override
    public Optional<ConfigWithMemberOfParliament> findExisting(SeriesConfig prepared) {
        return Optional.empty();
    }

}
