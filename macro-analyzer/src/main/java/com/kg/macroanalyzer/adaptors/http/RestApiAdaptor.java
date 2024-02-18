package com.kg.macroanalyzer.adaptors.http;

import com.kg.macroanalyzer.application.ports.driving.BuildChartDataParams;
import com.kg.macroanalyzer.application.ports.driving.ChartDataWithLabels;
import com.kg.macroanalyzer.application.ports.driving.DrivingPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("macro-analyzer")
public class RestApiAdaptor {

    private final DrivingPort drivingPort;

    @Autowired
    public RestApiAdaptor(DrivingPort drivingPort) {
        this.drivingPort = drivingPort;
    }

    @PostMapping("/chart-data")
    public ChartDataWithLabels buildChartData(@RequestBody BuildChartDataParams params) {
        logRequest(params);

        return drivingPort.buildChartData(params);
    }

    private void logRequest(BuildChartDataParams params) {
        final var msgRaw = "Received GET chart-data request with chartSeriesParams=%s";
        final var msgFormatted = msgRaw.formatted(params);
        log.info(msgFormatted);
    }

}
