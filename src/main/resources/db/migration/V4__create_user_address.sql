CREATE TABLE user_address (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    label           VARCHAR(50) NOT NULL,
    custom_label    VARCHAR(100),
    recipient_name  VARCHAR(100) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    full_address    TEXT NOT NULL,
    ward            VARCHAR(100),
    district        VARCHAR(100),
    city            VARCHAR(100),
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_address_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_user_address_user_id ON user_address (user_id);

