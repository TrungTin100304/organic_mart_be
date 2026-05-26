-- V5__add_storage_and_detailed_description_to_product.sql

ALTER TABLE product
    ADD COLUMN storage_instructions TEXT,
    ADD COLUMN detailed_description TEXT;

