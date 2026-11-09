# 0010. Own auth service with revocable sessions and JWTs

## Status

Accepted

## Date

2026-11-09

## Context

The project owner wants users to be able to log in, and later to comment. The goal is to run the app in Docker containers on one small computer, with open sign-up. Four Spring services already run there, so memory is limited. Nothing in the backend has authentication today, and [0004](0004-keep-manual-scrape-endpoints-public-for-now.md) names the manual scrape endpoints as a risk to close before the app is exposed.

## Decision

A fifth service, `auth-service`, on its own database, holds accounts, password credentials, sessions and the signing key.

- **Sign-up proves the address first.** Registering only takes an address. The account is created from the mailed link, which is also where the name and password are chosen. Nobody can claim an address they can't read, so there are no unverified accounts to take over.
- **Passwords** are hashed with Argon2id, at OWASP's minimum of 19 MiB and 2 iterations, at most four at once.
- **A session** is a random token in an `HttpOnly`, `Secure`, `SameSite=Strict` cookie limited to `/macro-analyzer/auth`, stored only as a hash. It can be revoked at once. A password reset ends all of a user's sessions.
- **The browser exchanges the session for a JWT** of 10 minutes (ES256), kept in memory and sent as a Bearer header. The services verify it with the public keys from `/macro-analyzer/auth/jwks.json` and call nothing per request. Because of the Bearer header, CSRF only concerns the auth routes.
- **Roles** are `USER` and `ADMIN`. The account with the address `AUTH_ADMIN_EMAIL` is an admin, so being an admin is proven the same way as owning an account, and no password is configured anywhere.
- **Mail** goes through SMTP. The mode `log` writes it to the service's log instead, for local work.
- The service uses `macro-platform`'s `shared` but not `scrape`: it has no queue, crons or scrape endpoints.

The project owner chose JWTs verified in every service, over the recommended gateway check of a session (nginx `auth_request`).

## Alternatives considered

- **nginx `auth_request` and trusted headers.** Less code in each service, and logging out takes effect at once. But a service would trust any caller that reaches its port, and each protected request would need a call to auth.
- **Keycloak or Authentik.** Complete, but about 1 to 2 GB of memory on a small machine (rough figures, not measured here).
- **Authelia.** Light, but has no self-registration.
- **JWT in an `HttpOnly` cookie.** No token handling in the frontend, but every state-changing route of every service would need CSRF protection.

## Consequences

- A revoked session or a changed role can still act until its JWT expires, at most 10 minutes.
- The signing key is stored unencrypted in the `auth` database, so that database's backups are as sensitive as the key.
- Open sign-up needs an SMTP provider and HTTPS before the app is exposed: the session cookie is `Secure`, and browsers only accept that over plain http on `localhost`.
- Rate limits are per client address at nginx. Behind a tunnel or another proxy, nginx needs `real_ip` configured, or all clients count as one.
- Comments will be personal data, and comments on parties can reveal political opinions, which GDPR treats as a special category. That needs handling before comments ship.
