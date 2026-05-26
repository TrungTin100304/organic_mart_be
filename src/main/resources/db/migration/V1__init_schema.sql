

CREATE TABLE users (
    id                          BIGSERIAL    PRIMARY KEY,
    full_name                   VARCHAR(255) NOT NULL,
    phone_number                VARCHAR(255) NOT NULL UNIQUE,
    email                       VARCHAR(255) NOT NULL UNIQUE,
    password_hash               VARCHAR(255),
    role                        VARCHAR(255),
    reset_password_token        VARCHAR(255),
    reset_password_expires_at   TIMESTAMP,
    created_at                  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_tokens (
    id          BIGSERIAL     PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    token       VARCHAR(2048) NOT NULL UNIQUE,
    expires_at  TIMESTAMP     NOT NULL,
    created_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);


CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
