package com.kg.macroanalyzer.auth.adaptors.postgres;

import com.kg.macroanalyzer.auth.application.port.out.AuthRepositoryPort;
import com.kg.macroanalyzer.auth.application.port.out.NewAccount;
import com.kg.macroanalyzer.auth.application.port.out.NewEmailToken;
import com.kg.macroanalyzer.auth.application.port.out.UserCredential;
import com.kg.macroanalyzer.auth.domain.DisplayNameTakenException;
import com.kg.macroanalyzer.auth.domain.EmailTokenPurpose;
import com.kg.macroanalyzer.auth.domain.Role;
import com.kg.macroanalyzer.auth.domain.User;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static com.kg.macroanalyzer.auth.jooq.generated.Tables.APP_USER;
import static com.kg.macroanalyzer.auth.jooq.generated.Tables.EMAIL_TOKEN;
import static com.kg.macroanalyzer.auth.jooq.generated.Tables.PASSWORD_CREDENTIAL;
import static com.kg.macroanalyzer.auth.jooq.generated.Tables.SIGNING_KEY;
import static com.kg.macroanalyzer.auth.jooq.generated.Tables.USER_SESSION;

@Repository
public class AuthPostgresAdaptor implements AuthRepositoryPort {

    private final DSLContext dsl;

    public AuthPostgresAdaptor(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public boolean userExists(String email) {
        return dsl.fetchExists(APP_USER, APP_USER.EMAIL.eq(email));
    }

    @Override
    public Optional<UserCredential> findCredential(String email) {
        return dsl.select(APP_USER.fields())
                .select(PASSWORD_CREDENTIAL.PASSWORD_HASH)
                .from(APP_USER)
                .join(PASSWORD_CREDENTIAL).on(PASSWORD_CREDENTIAL.USER_ID.eq(APP_USER.ID))
                .where(APP_USER.EMAIL.eq(email))
                .fetchOptional(row -> new UserCredential(
                        user(row), row.get(PASSWORD_CREDENTIAL.PASSWORD_HASH)));
    }

    @Override
    public boolean saveEmailTokenUnlessIssuedSince(NewEmailToken token, Instant issuedSince) {
        final var sameRecipient = EMAIL_TOKEN.EMAIL.eq(token.email())
                .and(EMAIL_TOKEN.PURPOSE.eq(token.purpose().name()));

        return dsl.transactionResult(configuration -> {
            final var transaction = DSL.using(configuration);
            transaction.deleteFrom(EMAIL_TOKEN)
                    .where(EMAIL_TOKEN.EXPIRES_AT.le(utc(token.created())))
                    .execute();
            if (transaction.fetchExists(
                    EMAIL_TOKEN, sameRecipient.and(EMAIL_TOKEN.CREATED.gt(utc(issuedSince))))) {
                return false;
            }
            transaction.deleteFrom(EMAIL_TOKEN).where(sameRecipient).execute();
            transaction.insertInto(EMAIL_TOKEN)
                    .set(EMAIL_TOKEN.TOKEN_HASH, token.tokenHash())
                    .set(EMAIL_TOKEN.EMAIL, token.email())
                    .set(EMAIL_TOKEN.PURPOSE, token.purpose().name())
                    .set(EMAIL_TOKEN.CREATED, utc(token.created()))
                    .set(EMAIL_TOKEN.EXPIRES_AT, utc(token.expiresAt()))
                    .execute();

            return true;
        });
    }

    @Override
    public boolean registerWithToken(
            String tokenHash, Instant now, NewAccount account, Function<String, Role> roleOfEmail) {
        try {
            return dsl.transactionResult(configuration -> {
                final var transaction = DSL.using(configuration);
                final var email =
                        consumeToken(transaction, tokenHash, EmailTokenPurpose.REGISTER, now);
                if (email.isEmpty()
                        || transaction.fetchExists(APP_USER, APP_USER.EMAIL.eq(email.get()))) {
                    return false;
                }
                if (transaction.fetchExists(APP_USER, displayNameIs(account.displayName()))) {
                    throw new DisplayNameTakenException();
                }
                transaction.insertInto(APP_USER)
                        .set(APP_USER.ID, account.id())
                        .set(APP_USER.EMAIL, email.get())
                        .set(APP_USER.DISPLAY_NAME, account.displayName())
                        .set(APP_USER.ROLE, roleOfEmail.apply(email.get()).name())
                        .set(APP_USER.CREATED, utc(now))
                        .execute();
                transaction.insertInto(PASSWORD_CREDENTIAL)
                        .set(PASSWORD_CREDENTIAL.USER_ID, account.id())
                        .set(PASSWORD_CREDENTIAL.PASSWORD_HASH, account.passwordHash())
                        .set(PASSWORD_CREDENTIAL.UPDATED, utc(now))
                        .execute();

                return true;
            });
        } catch (DuplicateKeyException e) {
            throw new DisplayNameTakenException();
        }
    }

    @Override
    public boolean resetPasswordWithToken(String tokenHash, Instant now, String passwordHash) {
        return dsl.transactionResult(configuration -> {
            final var transaction = DSL.using(configuration);
            final var userId = consumeToken(
                            transaction, tokenHash, EmailTokenPurpose.PASSWORD_RESET, now)
                    .flatMap(email -> transaction.select(APP_USER.ID)
                            .from(APP_USER)
                            .where(APP_USER.EMAIL.eq(email))
                            .fetchOptional(APP_USER.ID));
            if (userId.isEmpty()) {
                return false;
            }
            transaction.update(PASSWORD_CREDENTIAL)
                    .set(PASSWORD_CREDENTIAL.PASSWORD_HASH, passwordHash)
                    .set(PASSWORD_CREDENTIAL.UPDATED, utc(now))
                    .where(PASSWORD_CREDENTIAL.USER_ID.eq(userId.get()))
                    .execute();
            transaction.deleteFrom(USER_SESSION)
                    .where(USER_SESSION.USER_ID.eq(userId.get()))
                    .execute();

            return true;
        });
    }

    @Override
    public void saveSession(String tokenHash, UUID userId, Instant now, Instant expiresAt) {
        dsl.transaction(configuration -> {
            final var transaction = DSL.using(configuration);
            transaction.deleteFrom(USER_SESSION)
                    .where(USER_SESSION.EXPIRES_AT.le(utc(now)))
                    .execute();
            transaction.insertInto(USER_SESSION)
                    .set(USER_SESSION.TOKEN_HASH, tokenHash)
                    .set(USER_SESSION.USER_ID, userId)
                    .set(USER_SESSION.CREATED, utc(now))
                    .set(USER_SESSION.EXPIRES_AT, utc(expiresAt))
                    .execute();
        });
    }

    @Override
    public Optional<User> findUserOfSession(String tokenHash, Instant now) {
        return dsl.select(APP_USER.fields())
                .from(USER_SESSION)
                .join(APP_USER).on(APP_USER.ID.eq(USER_SESSION.USER_ID))
                .where(USER_SESSION.TOKEN_HASH.eq(tokenHash))
                .and(USER_SESSION.EXPIRES_AT.gt(utc(now)))
                .fetchOptional(AuthPostgresAdaptor::user);
    }

    @Override
    public void deleteSession(String tokenHash) {
        dsl.deleteFrom(USER_SESSION).where(USER_SESSION.TOKEN_HASH.eq(tokenHash)).execute();
    }

    @Override
    public void promoteToAdmin(String email) {
        dsl.update(APP_USER)
                .set(APP_USER.ROLE, Role.ADMIN.name())
                .where(APP_USER.EMAIL.eq(email))
                .execute();
    }

    @Override
    public List<String> signingKeysNewestFirst() {
        return dsl.select(SIGNING_KEY.PRIVATE_JWK)
                .from(SIGNING_KEY)
                .orderBy(SIGNING_KEY.CREATED.desc())
                .fetch(SIGNING_KEY.PRIVATE_JWK);
    }

    @Override
    public void saveSigningKey(String kid, String privateJwk, Instant now) {
        dsl.insertInto(SIGNING_KEY)
                .set(SIGNING_KEY.KID, kid)
                .set(SIGNING_KEY.PRIVATE_JWK, privateJwk)
                .set(SIGNING_KEY.CREATED, utc(now))
                .execute();
    }

    private static Optional<String> consumeToken(
            DSLContext transaction, String tokenHash, EmailTokenPurpose purpose, Instant now) {
        return transaction.deleteFrom(EMAIL_TOKEN)
                .where(EMAIL_TOKEN.TOKEN_HASH.eq(tokenHash))
                .and(EMAIL_TOKEN.PURPOSE.eq(purpose.name()))
                .and(EMAIL_TOKEN.EXPIRES_AT.gt(utc(now)))
                .returningResult(EMAIL_TOKEN.EMAIL)
                .fetchOptional(EMAIL_TOKEN.EMAIL);
    }

    private static Condition displayNameIs(String displayName) {
        return DSL.lower(APP_USER.DISPLAY_NAME).eq(DSL.lower(DSL.val(displayName)));
    }

    private static User user(Record row) {
        return new User(
                row.get(APP_USER.ID),
                row.get(APP_USER.EMAIL),
                row.get(APP_USER.DISPLAY_NAME),
                Role.valueOf(row.get(APP_USER.ROLE)));
    }

    private static OffsetDateTime utc(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }

}
