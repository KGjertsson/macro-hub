package com.kg.macroanalyzer.auth.domain;

import java.util.Locale;
import java.util.regex.Pattern;

public final class AccountRules {

    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MIN_PASSWORD_LENGTH = 12;
    // Hashing cost is fixed per call, and this keeps what a caller can hand to it bounded
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern DISPLAY_NAME = Pattern.compile(
            "^[\\p{L}\\p{N}][\\p{L}\\p{N} _-]{1,28}[\\p{L}\\p{N}]$");

    private AccountRules() {
    }

    public static String normalizedEmail(String email) {
        if (email == null) {
            throw new InvalidInputException("email");
        }
        final var normalized = email.strip().toLowerCase(Locale.ROOT);
        if (normalized.length() > MAX_EMAIL_LENGTH || !EMAIL.matcher(normalized).matches()) {
            throw new InvalidInputException("email");
        }

        return normalized;
    }

    public static String validPassword(String password) {
        if (password == null
                || password.length() < MIN_PASSWORD_LENGTH
                || password.length() > MAX_PASSWORD_LENGTH) {
            throw new InvalidInputException("password");
        }

        return password;
    }

    public static String validDisplayName(String displayName) {
        if (displayName == null || !DISPLAY_NAME.matcher(displayName.strip()).matches()) {
            throw new InvalidInputException("displayName");
        }

        return displayName.strip();
    }

}
