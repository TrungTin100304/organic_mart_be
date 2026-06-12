ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS payment_method VARCHAR(20) NOT NULL DEFAULT 'COD';

UPDATE orders
SET payment_method = 'VIETQR'
WHERE id IN (
    SELECT order_id
    FROM payment_request
    WHERE order_id IS NOT NULL
);
