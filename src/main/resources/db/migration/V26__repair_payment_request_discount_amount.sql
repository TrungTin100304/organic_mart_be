ALTER TABLE payment_request
    ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(15,2);

UPDATE payment_request
SET discount_amount = 0
WHERE discount_amount IS NULL;

ALTER TABLE payment_request
    ALTER COLUMN discount_amount SET DEFAULT 0,
    ALTER COLUMN discount_amount SET NOT NULL;
