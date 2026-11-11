package com.kg.macroanalyzer.auth.application.port.out;

import com.kg.macroanalyzer.auth.domain.Role;
import com.kg.macroanalyzer.auth.domain.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public interface AuthRepositoryPort {

    boolean userExists(String email);

    Optional<UserCredential> findCredential(String email);

    boolean saveEmailTokenUnlessIssuedSince(NewEmailToken token, Instant issuedSince);

    boolean registerWithToken(
            String tokenHash, Instant now, NewAccount account, Function<String, Role> roleOfEmail);

    boolean resetPasswordWithToken(String tokenHash, Instant now, String passwordHash);

    void saveSession(String tokenHash, UUID userId, Instant now, Instant expiresAt);

    Optional<User> findUserOfSession(String tokenHash, Instant now);

    void deleteSession(String tokenHash);

    void promoteToAdmin(String email);

    List<String> signingKeysNewestFirst();

    void saveSigningKey(String kid, String privateJwk, Instant now);

}
