package com.kg.macroanalyzer.auth.adaptors.http;

public record CompletePasswordResetRequest(String token, String password) {
}
