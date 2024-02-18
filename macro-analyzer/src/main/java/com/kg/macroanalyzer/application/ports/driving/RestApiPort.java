package com.kg.macroanalyzer.application.ports.driving;

import org.springframework.stereotype.Service;

@Service
public class RestApiPort implements DrivingPort {

    @Override
    public ChartDataWithLabels getChartData(String name, String country, String period) {
        return null;
    }

}
