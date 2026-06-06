BEGIN;

INSERT INTO product_category (name, slug, sort_order, created_at)
VALUES
    ('Rau củ quả', 'rau-cu-qua', 10, NOW()),
    ('Trái cây', 'trai-cay', 20, NOW()),
    ('Sữa & trứng', 'sua-trung', 30, NOW()),
    ('Ngũ cốc & hạt', 'ngu-coc-hat', 40, NOW()),
    ('Thực phẩm khô', 'thuc-pham-kho', 50, NOW())
ON CONFLICT (slug) DO UPDATE
SET name = EXCLUDED.name,
    sort_order = EXCLUDED.sort_order;

INSERT INTO allergen (name, description, created_at)
VALUES
    ('Gluten', 'Có thể không phù hợp với người nhạy cảm gluten.', NOW()),
    ('Dairy', 'Có thể chứa thành phần từ sữa.', NOW()),
    ('Peanuts', 'Có thể chứa đậu phộng hoặc vết đậu phộng.', NOW()),
    ('Tree Nuts', 'Có thể chứa các loại hạt cây như hạnh nhân, óc chó.', NOW()),
    ('Soy', 'Có thể chứa đậu nành hoặc dẫn xuất đậu nành.', NOW()),
    ('Eggs', 'Có thể chứa trứng hoặc dẫn xuất từ trứng.', NOW()),
    ('Sesame', 'Có thể chứa mè hoặc dầu mè.', NOW())
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description;

UPDATE farm
SET name = 'Đà Lạt Fresh Farm',
    certification = 'VietGAP Organic',
    location = 'Xã Xuân Thọ, Đà Lạt, Lâm Đồng',
    contact_phone = '0901000001'
WHERE contact_email = 'dalat.fresh@example.com';

INSERT INTO farm (name, certification, location, contact_phone, contact_email, created_at)
SELECT 'Đà Lạt Fresh Farm', 'VietGAP Organic', 'Xã Xuân Thọ, Đà Lạt, Lâm Đồng', '0901000001', 'dalat.fresh@example.com', NOW()
WHERE NOT EXISTS (SELECT 1 FROM farm WHERE contact_email = 'dalat.fresh@example.com');

UPDATE farm
SET name = 'Mekong Organic Farm',
    certification = 'Organic Vietnam',
    location = 'Phong Điền, Cần Thơ',
    contact_phone = '0901000002'
WHERE contact_email = 'mekong.organic@example.com';

INSERT INTO farm (name, certification, location, contact_phone, contact_email, created_at)
SELECT 'Mekong Organic Farm', 'Organic Vietnam', 'Phong Điền, Cần Thơ', '0901000002', 'mekong.organic@example.com', NOW()
WHERE NOT EXISTS (SELECT 1 FROM farm WHERE contact_email = 'mekong.organic@example.com');

UPDATE farm
SET name = 'Green Valley Farm',
    certification = 'USDA Organic',
    location = 'Củ Chi, TP. Hồ Chí Minh',
    contact_phone = '0901000003'
WHERE contact_email = 'green.valley@example.com';

INSERT INTO farm (name, certification, location, contact_phone, contact_email, created_at)
SELECT 'Green Valley Farm', 'USDA Organic', 'Củ Chi, TP. Hồ Chí Minh', '0901000003', 'green.valley@example.com', NOW()
WHERE NOT EXISTS (SELECT 1 FROM farm WHERE contact_email = 'green.valley@example.com');

WITH product_rows (
    category_slug,
    name,
    slug,
    description,
    storage_instructions,
    detailed_description,
    price,
    unit,
    nutrition_per_100g,
    image_url
) AS (
    VALUES
        (
            'rau-cu-qua',
            'Rau bó xôi Organic 300g',
            'rau-bo-xoi-organic-300g',
            'Rau bó xôi hữu cơ tươi, lá mềm, vị ngọt nhẹ, phù hợp cho salad, sinh tố xanh và món xào nhanh.',
            'Bảo quản lạnh 2-5°C, dùng tốt nhất trong 3 ngày sau khi nhận hàng.',
            'Thu hoạch trong ngày từ nông trại hữu cơ, rửa sơ bằng nước sạch trước khi chế biến.',
            32000,
            '300g',
            '{"energyKcal":23,"proteinG":2.9,"carbG":3.6,"fiberG":2.2}'::jsonb,
            'https://images.unsplash.com/photo-1576045057995-568f588f82fb?auto=format&fit=crop&w=900&q=80'
        ),
        (
            'rau-cu-qua',
            'Cải kale Organic 300g',
            'cai-kale-organic-300g',
            'Cải kale hữu cơ giòn, giàu chất xơ và vitamin K, dùng cho salad, nước ép hoặc áp chảo.',
            'Bảo quản lạnh 2-5°C trong túi kín, tránh để gần trái cây chín.',
            'Canh tác không thuốc trừ sâu tổng hợp, truy xuất theo từng lô thu hoạch.',
            36000,
            '300g',
            '{"energyKcal":49,"proteinG":4.3,"carbG":8.8,"fiberG":3.6}'::jsonb,
            'https://images.unsplash.com/photo-1524179091875-bf99a9a6af57?auto=format&fit=crop&w=900&q=80'
        ),
        (
            'rau-cu-qua',
            'Cà rốt Đà Lạt Organic 500g',
            'ca-rot-da-lat-organic-500g',
            'Cà rốt Đà Lạt hữu cơ, củ chắc, vị ngọt tự nhiên, thích hợp nấu súp, hầm hoặc ép nước.',
            'Bảo quản ngăn mát, tránh ánh nắng trực tiếp và nơi ẩm cao.',
            'Sản phẩm được chọn lọc theo kích thước đồng đều, đóng gói trong ngày nhập kho.',
            39000,
            '500g',
            '{"energyKcal":41,"proteinG":0.9,"carbG":9.6,"fiberG":2.8}'::jsonb,
            'https://images.unsplash.com/photo-1447175008436-054170c2e979?auto=format&fit=crop&w=900&q=80'
        ),
        (
            'rau-cu-qua',
            'Ớt chuông đỏ Organic 300g',
            'ot-chuong-do-organic-300g',
            'Ớt chuông đỏ hữu cơ giòn ngọt, màu sắc đẹp, phù hợp ăn sống, xào hoặc nướng.',
            'Bảo quản lạnh 4-8°C, dùng trong 5 ngày để giữ độ giòn.',
            'Không dùng chất bảo quản sau thu hoạch, phù hợp cho bữa ăn gia đình.',
            50000,
            '300g',
            '{"energyKcal":31,"proteinG":1,"carbG":6,"fiberG":2.1}'::jsonb,
            'https://images.unsplash.com/photo-1525607551316-4a8e16d1f9ba?auto=format&fit=crop&w=900&q=80'
        ),
        (
            'trai-cay',
            'Táo Fuji hữu cơ 500g',
            'tao-fuji-huu-co-500g',
            'Táo Fuji hữu cơ giòn, vị ngọt thanh, phù hợp ăn trực tiếp hoặc làm salad trái cây.',
            'Bảo quản lạnh 2-6°C, rửa sạch trước khi dùng.',
            'Nguồn hàng được kiểm tra ngoại quan và đóng gói theo lô nhập.',
            79000,
            '500g',
            '{"energyKcal":52,"proteinG":0.3,"carbG":14,"fiberG":2.4}'::jsonb,
            'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=900&q=80'
        ),
        (
            'trai-cay',
            'Chuối cau hữu cơ 1kg',
            'chuoi-cau-huu-co-1kg',
            'Chuối cau hữu cơ chín tự nhiên, trái nhỏ thơm, ngọt dịu, tiện dùng cho bữa phụ.',
            'Để nơi thoáng mát, tránh ánh nắng; khi chín có thể bảo quản lạnh.',
            'Không xử lý hóa chất thúc chín, độ chín có thể thay đổi theo ngày giao.',
            46000,
            '1kg',
            '{"energyKcal":89,"proteinG":1.1,"carbG":22.8,"fiberG":2.6}'::jsonb,
            'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?auto=format&fit=crop&w=900&q=80'
        ),
        (
            'ngu-coc-hat',
            'Sữa hạt hạnh nhân hữu cơ 1L',
            'sua-hat-hanh-nhan-huu-co-1l',
            'Sữa hạt hạnh nhân hữu cơ không đường, vị béo nhẹ, phù hợp dùng cùng ngũ cốc hoặc cà phê.',
            'Bảo quản lạnh sau khi mở nắp và dùng trong 3 ngày.',
            'Sản phẩm có chứa hạt cây, cần kiểm tra thông tin dị ứng trước khi sử dụng.',
            89000,
            '1L',
            '{"energyKcal":35,"proteinG":1.2,"carbG":1.5,"fatG":2.8}'::jsonb,
            'https://images.unsplash.com/photo-1550583724-b2692b85b150?auto=format&fit=crop&w=900&q=80'
        ),
        (
            'ngu-coc-hat',
            'Yến mạch cán dẹt hữu cơ 500g',
            'yen-mach-can-det-huu-co-500g',
            'Yến mạch cán dẹt hữu cơ, giàu chất xơ, dùng nấu cháo yến mạch, overnight oats hoặc làm bánh.',
            'Đậy kín sau khi mở, bảo quản nơi khô ráo, tránh ẩm.',
            'Được đóng gói trong bao bì kín để giữ hương vị và độ khô của hạt.',
            69000,
            '500g',
            '{"energyKcal":389,"proteinG":16.9,"carbG":66.3,"fiberG":10.6}'::jsonb,
            'https://images.unsplash.com/photo-1614961233913-a5113a4a34ed?auto=format&fit=crop&w=900&q=80'
        ),
        (
            'sua-trung',
            'Trứng gà thả vườn hữu cơ 10 quả',
            'trung-ga-tha-vuon-huu-co-10-qua',
            'Trứng gà thả vườn hữu cơ, lòng đỏ đậm, phù hợp cho bữa sáng, làm bánh và món gia đình.',
            'Bảo quản lạnh 2-8°C, dùng trước hạn in trên lô hàng.',
            'Trứng được phân loại, kiểm tra vỏ và đóng hộp trước khi giao.',
            65000,
            '10 quả',
            '{"energyKcal":143,"proteinG":12.6,"carbG":0.7,"fatG":9.5}'::jsonb,
            'https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?auto=format&fit=crop&w=900&q=80'
        ),
        (
            'thuc-pham-kho',
            'Nấm hương khô hữu cơ 100g',
            'nam-huong-kho-huu-co-100g',
            'Nấm hương khô hữu cơ thơm đậm, dùng cho món hầm, súp, lẩu hoặc xào rau củ.',
            'Bảo quản nơi khô ráo, đậy kín sau khi mở túi.',
            'Ngâm nước ấm 20-30 phút trước khi chế biến để nấm nở đều và mềm.',
            85000,
            '100g',
            '{"energyKcal":296,"proteinG":9.6,"carbG":75.4,"fiberG":11.5}'::jsonb,
            'https://images.unsplash.com/photo-1607877742574-a25d8b4b8f49?auto=format&fit=crop&w=900&q=80'
        )
)
INSERT INTO product (
    category_id,
    name,
    slug,
    description,
    storage_instructions,
    detailed_description,
    price,
    unit,
    nutrition_per_100g,
    image_url,
    is_active,
    created_at,
    updated_at
)
SELECT
    c.id,
    p.name,
    p.slug,
    p.description,
    p.storage_instructions,
    p.detailed_description,
    p.price,
    p.unit,
    p.nutrition_per_100g,
    p.image_url,
    TRUE,
    NOW(),
    NOW()
FROM product_rows p
JOIN product_category c ON c.slug = p.category_slug
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
    updated_at = NOW();

WITH product_allergen_rows (product_slug, allergen_name) AS (
    VALUES
        ('sua-hat-hanh-nhan-huu-co-1l', 'Tree Nuts'),
        ('yen-mach-can-det-huu-co-500g', 'Gluten'),
        ('trung-ga-tha-vuon-huu-co-10-qua', 'Eggs')
)
INSERT INTO product_allergen (product_id, allergen_id)
SELECT p.id, a.id
FROM product_allergen_rows r
JOIN product p ON p.slug = r.product_slug
JOIN allergen a ON a.name = r.allergen_name
ON CONFLICT DO NOTHING;

WITH batch_rows (
    product_slug,
    farm_name,
    batch_code,
    quantity_initial,
    quantity_remaining,
    import_date,
    expiry_date,
    cost_price
) AS (
    VALUES
        ('rau-bo-xoi-organic-300g', 'Đà Lạt Fresh Farm', 'SEED-SPINACH-001', 40.00, 32.00, CURRENT_DATE - INTERVAL '1 day', CURRENT_DATE + INTERVAL '6 days', 21000.00),
        ('cai-kale-organic-300g', 'Đà Lạt Fresh Farm', 'SEED-KALE-001', 35.00, 28.00, CURRENT_DATE - INTERVAL '1 day', CURRENT_DATE + INTERVAL '7 days', 24000.00),
        ('ca-rot-da-lat-organic-500g', 'Đà Lạt Fresh Farm', 'SEED-CARROT-001', 60.00, 48.00, CURRENT_DATE - INTERVAL '2 days', CURRENT_DATE + INTERVAL '18 days', 25000.00),
        ('ot-chuong-do-organic-300g', 'Green Valley Farm', 'SEED-BELLPEPPER-001', 30.00, 21.00, CURRENT_DATE - INTERVAL '1 day', CURRENT_DATE + INTERVAL '9 days', 34000.00),
        ('tao-fuji-huu-co-500g', 'Green Valley Farm', 'SEED-APPLE-001', 50.00, 44.00, CURRENT_DATE - INTERVAL '3 days', CURRENT_DATE + INTERVAL '25 days', 56000.00),
        ('chuoi-cau-huu-co-1kg', 'Mekong Organic Farm', 'SEED-BANANA-001', 80.00, 60.00, CURRENT_DATE - INTERVAL '1 day', CURRENT_DATE + INTERVAL '8 days', 29000.00),
        ('sua-hat-hanh-nhan-huu-co-1l', 'Green Valley Farm', 'SEED-ALMOND-MILK-001', 45.00, 35.00, CURRENT_DATE - INTERVAL '4 days', CURRENT_DATE + INTERVAL '45 days', 62000.00),
        ('yen-mach-can-det-huu-co-500g', 'Mekong Organic Farm', 'SEED-OATS-001', 55.00, 49.00, CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE + INTERVAL '180 days', 43000.00),
        ('trung-ga-tha-vuon-huu-co-10-qua', 'Mekong Organic Farm', 'SEED-EGGS-001', 70.00, 50.00, CURRENT_DATE - INTERVAL '1 day', CURRENT_DATE + INTERVAL '21 days', 45000.00),
        ('nam-huong-kho-huu-co-100g', 'Green Valley Farm', 'SEED-SHIITAKE-001', 25.00, 20.00, CURRENT_DATE - INTERVAL '6 days', CURRENT_DATE + INTERVAL '240 days', 58000.00)
)
INSERT INTO inventory_batch (
    product_id,
    farm_id,
    batch_code,
    quantity_initial,
    quantity_remaining,
    import_date,
    expiry_date,
    cost_price,
    created_at
)
SELECT
    p.id,
    f.id,
    b.batch_code,
    b.quantity_initial,
    b.quantity_remaining,
    b.import_date::date,
    b.expiry_date::date,
    b.cost_price,
    NOW()
FROM batch_rows b
JOIN product p ON p.slug = b.product_slug
JOIN farm f ON f.name = b.farm_name
ON CONFLICT (batch_code) DO UPDATE
SET product_id = EXCLUDED.product_id,
    farm_id = EXCLUDED.farm_id,
    quantity_initial = EXCLUDED.quantity_initial,
    quantity_remaining = EXCLUDED.quantity_remaining,
    import_date = EXCLUDED.import_date,
    expiry_date = EXCLUDED.expiry_date,
    cost_price = EXCLUDED.cost_price;

COMMIT;
