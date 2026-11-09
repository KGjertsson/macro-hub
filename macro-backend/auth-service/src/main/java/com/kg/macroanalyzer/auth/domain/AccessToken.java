package com.kg.macroanalyzer.auth.domain;

import java.time.Duration;

public record AccessToken(String value, Duration expiresIn) {
}
