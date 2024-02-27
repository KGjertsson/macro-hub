package com.kg.macroanalyzer.application.ports.driving;

import com.kg.macroanalyzer.application.domain.AlignedBundle;
import com.kg.macroanalyzer.application.ports.driving.chartdata.BuildChartDataParams;
import com.kg.macroanalyzer.application.ports.driving.seriesconfig.SeriesConfig;

import java.util.List;
import java.util.Optional;

public interface DrivingPort {

    Optional<AlignedBundle> buildAlignedBundle(BuildChartDataParams params);

    List<SeriesConfig> getSeriesConfigList();

}
