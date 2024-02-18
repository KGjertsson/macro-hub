package com.kg.macroanalyzer.adaptors.http;

import com.kg.macroanalyzer.application.ports.driving.ChartDataWithLabels;
import com.kg.macroanalyzer.application.ports.driving.DrivingPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/chart-data/")
    public ChartDataWithLabels getChartData(
            @RequestParam("name") String name,
            @RequestParam("country") String country,
            @RequestParam("period") String period
    ) {
        logRequest(name, country, period);

        return drivingPort.getChartData(name, country, period);
    }

    private void logRequest(String name, String country, String period) {
        final var msgRaw = "Received GET chart-data request with name=%s, country=%s, period=%s";
        final var msgFormatted = msgRaw.formatted(name, country, period);
        log.info(msgFormatted);
    }

}
