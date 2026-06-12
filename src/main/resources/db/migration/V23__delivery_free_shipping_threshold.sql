ALTER TABLE delivery_settings
    ADD COLUMN IF NOT EXISTS free_shipping_threshold DECIMAL(12,2);
