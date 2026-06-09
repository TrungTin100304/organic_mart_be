-- Fix check constraints on order_status_history table
-- The constraint order_status_history_from_status_check was created by a legacy migration
-- (or Hibernate DDL) that doesn't include 'PREPARING', causing violations when
-- Admins update orders from CONFIRMED -> PREPARING -> READY_FOR_DELIVERY.

-- Fix from_status: allow NULL (initial state) and all valid OrderStatus values
ALTER TABLE order_status_history
    DROP CONSTRAINT IF EXISTS order_status_history_from_status_check;

ALTER TABLE order_status_history
    ADD CONSTRAINT order_status_history_from_status_check
    CHECK (from_status IS NULL OR from_status IN (
        'PENDING', 'CONFIRMED', 'PREPARING', 'READY_FOR_DELIVERY',
        'DELIVERING', 'DELIVERED', 'CANCELLED', 'REFUNDED'
    ));

-- Ensure to_status has correct constraint (covers all valid OrderStatus values)
ALTER TABLE order_status_history
    DROP CONSTRAINT IF EXISTS order_status_history_to_status_check;

ALTER TABLE order_status_history
    ADD CONSTRAINT order_status_history_to_status_check
    CHECK (to_status IN (
        'PENDING', 'CONFIRMED', 'PREPARING', 'READY_FOR_DELIVERY',
        'DELIVERING', 'DELIVERED', 'CANCELLED', 'REFUNDED'
    ));
