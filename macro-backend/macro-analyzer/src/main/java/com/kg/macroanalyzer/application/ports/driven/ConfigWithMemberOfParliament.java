package com.kg.macroanalyzer.application.ports.driven;

import com.kg.macroanalyzer.application.domain.parliament.MemberOfParliament;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record ConfigWithMemberOfParliament(
        SeriesConfig seriesConfig,
        List<MemberOfParliament> membersOfParliament
) {
}
