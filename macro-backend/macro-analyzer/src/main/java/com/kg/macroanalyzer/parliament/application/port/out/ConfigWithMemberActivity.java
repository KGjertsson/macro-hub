package com.kg.macroanalyzer.parliament.application.port.out;

import com.kg.macroanalyzer.parliament.domain.MemberActivity;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record ConfigWithMemberActivity(
        SeriesConfig seriesConfig,
        List<String> intressentIds,
        List<MemberActivity> memberActivity
) {
}
