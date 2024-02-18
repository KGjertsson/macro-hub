package com.kg.macroanalyzer.application.ports.driving;

public interface DrivingPort {

    ChartDataWithLabels getChartData(String name, String country, String period);

}
