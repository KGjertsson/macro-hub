package com.kg.macroanalyzer.application.ports.driving;

import com.kg.macroanalyzer.application.domain.AlignedBundle;

import java.util.Optional;

public interface DrivingPort {

    Optional<AlignedBundle> buildAlignedBundle(BuildChartDataParams params);

}
