-- V15__add_more_products_for_home.sql

-- Adjust created_at and updated_at of existing products to have different timestamps
-- This will ensure proper sorting for "Sản phẩm mới về"
UPDATE product SET created_at = NOW() - INTERVAL '10 days', updated_at = NOW() - INTERVAL '10 days' WHERE slug = 'rau-bo-xoi-organic-300g';
UPDATE product SET created_at = NOW() - INTERVAL '9 days', updated_at = NOW() - INTERVAL '9 days' WHERE slug = 'cai-kale-organic-300g';
UPDATE product SET created_at = NOW() - INTERVAL '8 days', updated_at = NOW() - INTERVAL '8 days' WHERE slug = 'ca-rot-da-lat-organic-500g';
UPDATE product SET created_at = NOW() - INTERVAL '7 days', updated_at = NOW() - INTERVAL '7 days' WHERE slug = 'ot-chuong-do-organic-300g';
UPDATE product SET created_at = NOW() - INTERVAL '6 days', updated_at = NOW() - INTERVAL '6 days' WHERE slug = 'tao-fuji-huu-co-500g';
UPDATE product SET created_at = NOW() - INTERVAL '5 days', updated_at = NOW() - INTERVAL '5 days' WHERE slug = 'chuoi-cau-huu-co-1kg';
UPDATE product SET created_at = NOW() - INTERVAL '4 days', updated_at = NOW() - INTERVAL '4 days' WHERE slug = 'sua-hat-hanh-nhan-huu-co-1l';
UPDATE product SET created_at = NOW() - INTERVAL '3 days', updated_at = NOW() - INTERVAL '3 days' WHERE slug = 'yen-mach-can-det-huu-co-500g';
UPDATE product SET created_at = NOW() - INTERVAL '2 days', updated_at = NOW() - INTERVAL '2 days' WHERE slug = 'trung-ga-tha-vuon-huu-co-10-qua';
UPDATE product SET created_at = NOW() - INTERVAL '1 day', updated_at = NOW() - INTERVAL '1 day' WHERE slug = 'nam-huong-kho-huu-co-100g';

-- Insert 2 new products to bring total to 12
-- Product 1: Dâu tây Đà Lạt Organic 250g (Category: trai-cay-tuoi)
-- Product 2: Mật ong hoa rừng Organic 500ml (Category: thuc-pham-kho)
INSERT INTO product (category_id, name, slug, description, storage_instructions, detailed_description, price, unit, nutrition_per_100g, image_url, is_active, created_at, updated_at)
VALUES 
    ((SELECT id FROM product_category WHERE slug = 'trai-cay-tuoi' LIMIT 1), 'Dâu tây Đà Lạt Organic 250g', 'dau-tay-da-lat-organic-250g', 'Dâu tây Đà Lạt hữu cơ chín mọng, vị chua ngọt hài hòa, giàu vitamin C và chất chống oxy hóa.', 'Bảo quản lạnh 2-4°C, tránh va đập mạnh và rửa trước khi dùng.', 'Được trồng theo mô hình treo giàn trong nhà màng hữu cơ, không sử dụng chất kích thích sinh trưởng.', 95000.00, '250g', '{"energyKcal":32,"proteinG":0.7,"carbG":7.7,"fiberG":2}'::jsonb, 'https://images.unsplash.com/photo-1464965911861-746a04b4bca6?auto=format&fit=crop&w=900&q=80', TRUE, NOW() - INTERVAL '12 hours', NOW() - INTERVAL '12 hours'),
    ((SELECT id FROM product_category WHERE slug = 'thuc-pham-kho' LIMIT 1), 'Mật ong hoa rừng Organic 500ml', 'mat-ong-hoa-rung-organic-500ml', 'Mật ong hoa rừng nguyên chất hữu cơ, màu vàng óng, vị ngọt thanh tự nhiên.', 'Bảo quản nơi khô ráo, thoáng mát, không cần để trong tủ lạnh.', 'Thu hoạch từ các tổ ong nuôi tự nhiên tại vườn quốc gia, cam kết không pha đường hay chất bảo quản.', 150000.00, '500ml', '{"energyKcal":304,"proteinG":0.3,"carbG":82.4,"fatG":0}'::jsonb, 'https://images.unsplash.com/photo-1587049352846-4a222e784d38?auto=format&fit=crop&w=900&q=80', TRUE, NOW(), NOW())
ON CONFLICT (slug) DO UPDATE
SET category_id = EXCLUDED.category_id,
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    storage_instructions = EXCLUDED.storage_instructions,
    detailed_description = EXCLUDED.detailed_description,
    price = EXCLUDED.price,
    unit = EXCLUDED.unit,
    nutrition_per_100g = EXCLUDED.nutrition_per_100g,
    image_url = EXCLUDED.image_url,
    is_active = TRUE,
    updated_at = EXCLUDED.updated_at;

-- Insert inventory batches for the 2 new products
INSERT INTO inventory_batch (product_id, farm_id, batch_code, quantity_initial, quantity_remaining, import_date, expiry_date, cost_price, created_at)
VALUES
    ((SELECT id FROM product WHERE slug = 'dau-tay-da-lat-organic-250g' LIMIT 1), (SELECT id FROM farm WHERE name = 'Đà Lạt Fresh Farm' LIMIT 1), 'SEED-STRAWBERRY-001', 50.00, 45.00, (CURRENT_DATE - INTERVAL '1 day')::date, (CURRENT_DATE + INTERVAL '5 days')::date, 65000.00, NOW() - INTERVAL '12 hours'),
    ((SELECT id FROM product WHERE slug = 'mat-ong-hoa-rung-organic-500ml' LIMIT 1), (SELECT id FROM farm WHERE name = 'Mekong Organic Farm' LIMIT 1), 'SEED-HONEY-001', 40.00, 38.00, (CURRENT_DATE - INTERVAL '3 days')::date, (CURRENT_DATE + INTERVAL '180 days')::date, 100000.00, NOW())
ON CONFLICT (batch_code) DO UPDATE
SET product_id = EXCLUDED.product_id,
    farm_id = EXCLUDED.farm_id,
    quantity_initial = EXCLUDED.quantity_initial,
    quantity_remaining = EXCLUDED.quantity_remaining,
    import_date = EXCLUDED.import_date,
    expiry_date = EXCLUDED.expiry_date,
    cost_price = EXCLUDED.cost_price;
