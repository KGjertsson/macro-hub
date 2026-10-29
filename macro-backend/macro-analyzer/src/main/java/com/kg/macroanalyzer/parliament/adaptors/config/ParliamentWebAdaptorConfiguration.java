package com.kg.macroanalyzer.parliament.adaptors.config;

import com.kg.macroanalyzer.parliament.adaptors.web.MembersOfParliamentWebAdaptor;
import com.kg.macroanalyzer.parliament.adaptors.web.RiksdagenActivityWebAdaptor;
import com.kg.macroanalyzer.parliament.application.port.out.MemberActivityRequest;
import com.kg.macroanalyzer.parliament.domain.MemberActivity;
import com.kg.macroanalyzer.parliament.domain.MemberOfParliament;
import com.kg.macroanalyzer.scrape.domain.SeriesConfig;
import com.kg.macroanalyzer.shared.web.WebPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Stream;

@Configuration
public class ParliamentWebAdaptorConfiguration {

    @Bean("memberWebAdaptor")
    public WebPort<SeriesConfig, Stream<MemberOfParliament>> memberWebAdaptor() {
        return new MembersOfParliamentWebAdaptor();
    }

    @Bean("memberActivityWebAdaptor")
    public WebPort<MemberActivityRequest, MemberActivity> memberActivityWebAdaptor() {
        return new RiksdagenActivityWebAdaptor();
    }

}
