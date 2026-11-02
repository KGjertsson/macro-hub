package com.kg.macroanalyzer.parliament.application.port.out;

import com.kg.macroanalyzer.parliament.domain.MemberOfParliament;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record ConfigWithMemberOfParliament(
        SeriesConfig seriesConfig,
        List<MemberOfParliament> membersOfParliament
) {
}
