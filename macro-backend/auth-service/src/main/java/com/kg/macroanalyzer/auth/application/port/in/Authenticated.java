package com.kg.macroanalyzer.auth.application.port.in;

import com.kg.macroanalyzer.auth.domain.AccessToken;
import com.kg.macroanalyzer.auth.domain.User;

public record Authenticated(AccessToken accessToken, User user) {
}
