ALTER TABLE payment_request
    ADD COLUMN IF NOT EXISTS discount_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS promotion_id BIGINT;

ALTER TABLE payment_request
    ADD CONSTRAINT fk_payment_request_promotion
    FOREIGN KEY (promotion_id) REFERENCES promotion(id) ON DELETE SET NULL;

ALTER TABLE promotion_usage
    ALTER COLUMN order_id DROP NOT NULL,
    ADD COLUMN IF NOT EXISTS payment_request_id BIGINT;

ALTER TABLE promotion_usage
    ADD CONSTRAINT fk_promotion_usage_payment
    FOREIGN KEY (payment_request_id) REFERENCES payment_request(id) ON DELETE CASCADE;

CREATE UNIQUE INDEX IF NOT EXISTS uk_promotion_usage_payment
    ON promotion_usage(payment_request_id)
    WHERE payment_request_id IS NOT NULL;
