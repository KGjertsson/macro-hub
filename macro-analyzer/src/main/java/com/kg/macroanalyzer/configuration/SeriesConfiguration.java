package com.kg.macroanalyzer.configuration;

import com.kg.macroanalyzer.application.ports.driving.out.OutPort;
import com.kg.macroanalyzer.application.ports.driving.out.seriesconfig.SeriesConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SeriesConfiguration {

    private final OutPort outPort;

    public SeriesConfiguration(OutPort outPort) {
        this.outPort = outPort;
    }

    @Bean
    public List<SeriesConfig> getSeriesConfigList() {
        return outPort.getSeriesConfigList();
    }

}
