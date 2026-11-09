package com.kg.macroanalyzer.auth.adaptors.http;

import com.kg.macroanalyzer.auth.domain.Role;

import java.util.UUID;

public record AuthenticatedResponse(String accessToken, long expiresInSeconds, UserResponse user) {

    public record UserResponse(UUID id, String email, String displayName, Role role) {
    }

}
