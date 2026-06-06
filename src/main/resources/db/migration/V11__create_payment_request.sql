CREATE TABLE payment_request (
    id            BIGSERIAL       PRIMARY KEY,
    user_id       BIGINT          NOT NULL,
    address_id    BIGINT          NOT NULL,
    subtotal      NUMERIC(15, 2)  NOT NULL,
    shipping_fee  NUMERIC(15, 2)  NOT NULL,
    amount        NUMERIC(15, 2)  NOT NULL,
    transfer_code VARCHAR(50)     NOT NULL UNIQUE,
    qr_url        TEXT            NOT NULL,
    status        VARCHAR(20)     NOT NULL,
    created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at    TIMESTAMP       NOT NULL,
    CONSTRAINT fk_payment_request_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_request_address
        FOREIGN KEY (address_id) REFERENCES user_address (id) ON DELETE RESTRICT
);

CREATE INDEX idx_payment_request_user_id ON payment_request (user_id);
CREATE INDEX idx_payment_request_status ON payment_request (status);
