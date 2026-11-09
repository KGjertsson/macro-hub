package com.kg.macroanalyzer.auth.domain;

import java.util.UUID;

public record User(UUID id, String email, String displayName, Role role) {
}
