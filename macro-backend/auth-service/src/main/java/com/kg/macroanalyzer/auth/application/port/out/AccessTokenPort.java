package com.kg.macroanalyzer.auth.application.port.out;

import com.kg.macroanalyzer.auth.domain.AccessToken;
import com.kg.macroanalyzer.auth.domain.User;

import java.time.Instant;

public interface AccessTokenPort {

    AccessToken issue(User user, Instant now);

    String publicKeys();

}
