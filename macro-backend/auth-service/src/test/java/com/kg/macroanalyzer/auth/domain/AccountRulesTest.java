package com.kg.macroanalyzer.auth.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountRulesTest {

    @Test
    void shouldLowerCaseAndTrimAnEmail() {
        assertEquals("anna@example.se", AccountRules.normalizedEmail("  Anna@Example.SE "));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "anna", "anna@", "@example.se", "anna@example", "a b@example.se", "a@b@example.se"})
    void shouldRejectAnInvalidEmail(String email) {
        assertThrows(InvalidInputException.class, () -> AccountRules.normalizedEmail(email));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "elevenchars"})
    void shouldRejectAShortPassword(String password) {
        assertThrows(InvalidInputException.class, () -> AccountRules.validPassword(password));
    }

    @Test
    void shouldRejectAPasswordBeyondTheLimit() {
        assertThrows(InvalidInputException.class, () -> AccountRules.validPassword("x".repeat(129)));
    }

    @Test
    void shouldAcceptSwedishDisplayNames_andTrimThem() {
        assertEquals("Åsa Öberg-Ek", AccountRules.validDisplayName(" Åsa Öberg-Ek "));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "ab", "<script>", " -start"})
    void shouldRejectAnInvalidDisplayName(String displayName) {
        assertThrows(InvalidInputException.class, () -> AccountRules.validDisplayName(displayName));
    }

}
