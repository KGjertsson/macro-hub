CREATE TABLE app_user
(
    id           uuid PRIMARY KEY,
    email        text        NOT NULL UNIQUE,
    display_name text        NOT NULL,
    role         text        NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    created      timestamptz NOT NULL
);

CREATE UNIQUE INDEX app_user_display_name_key ON app_user (lower(display_name));

-- Apart from app_user so that another way of logging in can be added next to it
CREATE TABLE password_credential
(
    user_id       uuid PRIMARY KEY REFERENCES app_user (id) ON DELETE CASCADE,
    password_hash text        NOT NULL,
    updated       timestamptz NOT NULL
);

CREATE TABLE user_session
(
    token_hash text PRIMARY KEY,
    user_id    uuid        NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    created    timestamptz NOT NULL,
    expires_at timestamptz NOT NULL
);

CREATE INDEX user_session_user_id ON user_session (user_id);

CREATE TABLE email_token
(
    token_hash text PRIMARY KEY,
    email      text        NOT NULL,
    purpose    text        NOT NULL CHECK (purpose IN ('REGISTER', 'PASSWORD_RESET')),
    created    timestamptz NOT NULL,
    expires_at timestamptz NOT NULL
);

CREATE INDEX email_token_email_purpose ON email_token (email, purpose);

CREATE TABLE signing_key
(
    kid         text PRIMARY KEY,
    private_jwk text        NOT NULL,
    created     timestamptz NOT NULL
);
