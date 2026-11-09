package com.kg.macroanalyzer.auth.application.port.out;

import com.kg.macroanalyzer.auth.domain.User;

public record UserCredential(User user, String passwordHash) {
}
