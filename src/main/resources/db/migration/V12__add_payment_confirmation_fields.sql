ALTER TABLE payment_request
    ADD COLUMN transaction_id VARCHAR(100) UNIQUE,
    ADD COLUMN paid_at TIMESTAMP;
