package com.kg.macroanalyzer.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JacksonConfiguration {

    // The frontend reads date labels as arrays (e.g. [1994,6]), which was the Jackson 2 default
    @Bean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .build();
    }

}
