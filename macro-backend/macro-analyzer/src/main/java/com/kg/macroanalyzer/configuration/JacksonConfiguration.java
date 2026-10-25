package com.kg.macroanalyzer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JacksonConfiguration implements WebMvcConfigurer {

    // The frontend reads date labels as arrays (e.g. [1994,6]), which was the Jackson 2 default
    @Bean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .build();
    }

    // @EnableWebMvc turns off Boot's MVC auto-configuration, so register the mapper explicitly
    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        builder.withJsonConverter(new JacksonJsonHttpMessageConverter(jsonMapper()));
    }

}
