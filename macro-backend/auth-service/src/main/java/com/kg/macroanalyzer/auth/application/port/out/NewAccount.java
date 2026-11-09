package com.kg.macroanalyzer.auth.application.port.out;

import java.util.UUID;

public record NewAccount(UUID id, String displayName, String passwordHash) {
}
