CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    product_id BIGINT NOT NULL REFERENCES product(id),
    order_id BIGINT REFERENCES orders(id),
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    review_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    report_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

