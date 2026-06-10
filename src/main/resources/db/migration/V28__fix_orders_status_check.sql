-- Fix orders check constraint to include all valid order statuses.
-- PREPARING was added to OrderStatus enum but the DB constraint was never updated.

ALTER TABLE orders
DROP CONSTRAINT IF EXISTS orders_status_check;

ALTER TABLE orders
ADD CONSTRAINT orders_status_check
CHECK (status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'READY_FOR_DELIVERY', 'DELIVERING', 'DELIVERED', 'CANCELLED', 'REFUNDED'));
