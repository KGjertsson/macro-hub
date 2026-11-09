package com.kg.macroanalyzer.auth.application.port.in;

public interface AuthCommands {

    void requestRegistration(String email);

    void completeRegistration(String emailToken, String displayName, String password);

    void requestPasswordReset(String email);

    void completePasswordReset(String emailToken, String password);

    Login login(String email, String password);

    Authenticated refresh(String sessionToken);

    void logout(String sessionToken);

    String publicKeys();

}
