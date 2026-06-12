-- sepay_webhook_event table: stores every inbound webhook for auditability and idempotency
CREATE TABLE sepay_webhook_event (
    id                     BIGSERIAL       PRIMARY KEY,
    sepay_transaction_id   VARCHAR(100)    NOT NULL UNIQUE,
    reference_code         VARCHAR(100),
    transfer_code          VARCHAR(100),
    transfer_amount        NUMERIC(15, 2)  NOT NULL,
    account_number         VARCHAR(50),
    transfer_type          VARCHAR(20),
    gateway               VARCHAR(50),
    raw_payload           TEXT,
    status                VARCHAR(20)     NOT NULL DEFAULT 'RECEIVED',
    rejection_reason      VARCHAR(255),
    processed_at          TIMESTAMP,
    created_at            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sepay_event_tx_id   ON sepay_webhook_event (sepay_transaction_id);
CREATE INDEX idx_sepay_event_code   ON sepay_webhook_event (transfer_code);
CREATE INDEX idx_sepay_event_status ON sepay_webhook_event (status);

-- Add payment_items_snapshot to store cart items at payment time
ALTER TABLE payment_request
    ADD COLUMN payment_items_snapshot TEXT;

-- Add order link to payment_request
ALTER TABLE payment_request
    ADD COLUMN order_id BIGINT;

ALTER TABLE payment_request
    ADD CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL;

-- Add shipping_provider_id to payment_request
ALTER TABLE payment_request
    ADD COLUMN shipping_provider_id BIGINT;
