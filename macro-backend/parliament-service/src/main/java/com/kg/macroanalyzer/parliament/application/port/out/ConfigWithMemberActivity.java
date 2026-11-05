package com.kg.macroanalyzer.parliament.application.port.out;

import com.kg.macroanalyzer.parliament.domain.MemberActivity;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import com.kg.macroanalyzer.shared.collection.ImmutableCopies;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record ConfigWithMemberActivity(
        SeriesConfig seriesConfig,
        List<String> intressentIds,
        List<MemberActivity> memberActivity
) {

    public ConfigWithMemberActivity {
        intressentIds = ImmutableCopies.list(intressentIds);
        memberActivity = ImmutableCopies.list(memberActivity);
    }

}
