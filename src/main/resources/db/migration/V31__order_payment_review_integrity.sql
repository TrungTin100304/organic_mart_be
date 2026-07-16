CREATE UNIQUE INDEX IF NOT EXISTS uk_payment_request_order
    ON payment_request(order_id)
    WHERE order_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_review_user_product
    ON reviews(user_id, product_id);

CREATE INDEX IF NOT EXISTS idx_inventory_batch_product_expiry_available
    ON inventory_batch(product_id, expiry_date, id)
    WHERE quantity_remaining > 0;
