package com.kg.macroanalyzer.application.ports.driving;

import java.util.Optional;

public interface DrivingPort {

    Optional<ChartDataWithLabels> buildChartData(BuildChartDataParams params);

}
