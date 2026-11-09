package com.kg.macroanalyzer.auth.application.port.in;

import java.time.Duration;

public record Login(String sessionToken, Duration sessionTtl, Authenticated authenticated) {
}
