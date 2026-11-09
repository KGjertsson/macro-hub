package com.kg.macroanalyzer.auth.application.port.out;

public interface PasswordHasherPort {

    String hash(String password);

    boolean matches(String password, String hash);

}
