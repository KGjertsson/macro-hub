package com.kg.macroanalyzer.auth.adaptors.http;

public record CompleteRegistrationRequest(String token, String displayName, String password) {
}
