-- Import the attached catalog snapshot without duplicating existing products.
BEGIN;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM product_category
        WHERE id = 4 AND parent_id IS NULL AND slug = 'gia-vi-nguyen-vat-lieu-nau-an'
    ) THEN
        RAISE EXCEPTION 'Expected root category id=4 slug=gia-vi-nguyen-vat-lieu-nau-an';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM product_category
        WHERE id = 6 AND parent_id = 1 AND slug = 'rau-an-la'
    ) THEN
        RAISE EXCEPTION 'Expected category id=6 parent_id=1 slug=rau-an-la';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM product_category
        WHERE id = 7 AND parent_id = 1 AND slug = 'rau-an-cu'
    ) THEN
        RAISE EXCEPTION 'Expected category id=7 parent_id=1 slug=rau-an-cu';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM product_category
        WHERE id = 8 AND parent_id = 1 AND slug = 'rau-an-qua'
    ) THEN
        RAISE EXCEPTION 'Expected category id=8 parent_id=1 slug=rau-an-qua';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM product_category
        WHERE id = 12 AND parent_id = 2 AND slug = 'trai-cay-trong-nuoc'
    ) THEN
        RAISE EXCEPTION 'Expected category id=12 parent_id=2 slug=trai-cay-trong-nuoc';
    END IF;
END $$;

CREATE TEMP TABLE attached_product_import (
    slug TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    category_id BIGINT NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    unit TEXT NOT NULL,
    image_url TEXT NOT NULL
) ON COMMIT DROP;

INSERT INTO attached_product_import (slug, name, category_id, price, unit, image_url)
VALUES
    ('trung-ga-ta-sach-10-qua', 'Trứng gà ta sạch - 10 quả', 4, 75000, '10 quả', 'https://bizweb.dktcdn.net/100/390/808/products/7dc26e6782aedbf8f16f9d8d0e316f87.jpg?v=1604733176453'),
    ('tac-quat-organic-300gr', 'Tắc ( Quất ) organic 300gr', 12, 30000, '300gr', 'https://bizweb.dktcdn.net/100/390/808/products/trai-tac-1kg.jpg?v=1600505273100'),
    ('khoai-lang-nhat-organic-300gr', 'Khoai Lang Nhật  Organic - 300gr', 7, 27000, '300gr', 'https://bizweb.dktcdn.net/100/390/808/products/khoai-lang-huu-co-500x500.jpg?v=1600505034837'),
    ('dau-bap-organic-300gr', 'Đậu Bắp Organic 300gr', 8, 27000, '300gr', 'https://bizweb.dktcdn.net/100/390/808/products/dau-bap-huu-co-500x500.jpg?v=1600504946570'),
    ('bap-chuoi-organic-1kg', 'Bắp Chuối Organic 1kg', 6, 45000, '1kg', 'https://bizweb.dktcdn.net/100/390/808/products/hoa-chuoi-huu-co-500x500.jpg?v=1600504848320'),
    ('cai-tatsoi-cai-hoa-hong-organic-250gr', 'Cải Tatsoi ( Cải Hoa Hồng ) Organic 250gr', 6, 27000, '250gr', 'https://bizweb.dktcdn.net/100/390/808/products/58d96e4f4ac2d512db50144d4d677c034301b086.jpg?v=1600504762873'),
    ('xa-lach-mo-organic-300gr', 'Xà Lách Mỡ Organic 300gr', 6, 30000, '300gr', 'https://bizweb.dktcdn.net/100/390/808/products/2003d66491a1c8fc3e629034b0288a74.jpg?v=1600504580920'),
    ('xa-lach-lo-lo-organic-300gr', 'Xà Lách Lo Lo Organic 300gr', 6, 30000, '300gr', 'https://bizweb.dktcdn.net/100/390/808/products/xa-lach-lolo-xanh-500x500.jpg?v=1600504442550'),
    ('dua-leo-organic-300gr', 'Dưa Leo Organic 300gr', 8, 27000, '300gr', 'https://bizweb.dktcdn.net/100/390/808/products/16429764214814.jpg?v=1600504354567'),
    ('ca-tim-organic-300gr', 'Cà Tím Organic 300gr', 8, 26000, '300gr', 'https://bizweb.dktcdn.net/100/390/808/products/20190405174004hat-giong-ca-tim.png?v=1600504262433'),
    ('rau-ma-organic-300gr', 'Rau má Organic 300gr', 6, 35000, '300gr', 'https://bizweb.dktcdn.net/100/390/808/products/nu-c-p-rau-m-min.jpg?v=1600504031593'),
    ('bap-nep-organic-300gr', 'Bắp Nếp Organic 300gr', 8, 23000, '300gr', 'https://bizweb.dktcdn.net/100/390/808/products/hat-giong-bap-nep-nu-1-2.jpg?v=1600503780137'),
    ('khoai-mon-sap-vang-organic-300gr', 'Khoai môn sáp vàng Organic 300gr', 7, 26000, '300gr', 'https://bizweb.dktcdn.net/100/390/808/products/faf6b495f2c24260b79acdd95099008a-4a337a83364b43a59be3cb4b815496fd-master.jpg?v=1593163116783'),
    ('khoai-mo-organic-300gr', 'Khoai mỡ Organic 300gr', 7, 27000, '300gr', 'https://bizweb.dktcdn.net/100/390/808/products/13405198-jpeg.jpg?v=1593163339683'),
    ('cu-cai-do-300gr', 'Củ cải đỏ 300gr', 7, 24000, '300gr', 'https://bizweb.dktcdn.net/100/390/808/products/cu-cai-duong-huu-co-500x500.jpg?v=1592813122220'),
    ('cu-cai-trang-organic-300gr', 'Củ cải trắng Organic 300gr', 7, 30000, '300gr', 'https://bizweb.dktcdn.net/100/390/808/products/847184545894.jpg?v=1592812267877'),
    ('cu-hoi-organic-500gr', 'Củ hồi Organic 500gr', 7, 47500, '500gr', 'https://bizweb.dktcdn.net/100/390/808/products/upload-9bdbd4d6855d438fae803c3fbf56e5dd-large.jpg?v=1592810724327'),
    ('cu-den-organic-300gr', 'Củ dền Organic 300gr', 7, 30000, '300gr', 'https://bizweb.dktcdn.net/100/390/808/products/cu-den-do-baby-large.jpg?v=1593855273587'),
    ('su-hao-trang-organic-300g', 'Su hào trắng Organic 300g', 7, 27000, '300g', 'https://bizweb.dktcdn.net/100/390/808/products/product2263.jpg?v=1592366623437'),
    ('khoai-lang-mat-300gr', 'Khoai lang mật - 300gr', 7, 19000, '300gr', 'https://bizweb.dktcdn.net/100/390/808/products/77158-nguyen-lieu-lam-mut-khoai-lang-kho-gion-700x704.jpg?v=1592366775133');

DO $$
DECLARE
    collision RECORD;
BEGIN
    SELECT
        imported.name AS product_name,
        existing.slug AS existing_slug,
        imported.slug AS import_slug
    INTO collision
    FROM attached_product_import imported
    JOIN product existing
        ON LOWER(TRIM(existing.name)) = LOWER(TRIM(imported.name))
       AND existing.slug <> imported.slug
    LIMIT 1;

    IF FOUND THEN
        RAISE EXCEPTION 'Attached product name collision: "%" uses slug "%", expected "%"',
            collision.product_name,
            collision.existing_slug,
            collision.import_slug;
    END IF;
END $$;

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
    imported.category_id,
    imported.name,
    imported.slug,
    'Sản phẩm hữu cơ tươi sạch từ Organic Mart.',
    NULL,
    NULL,
    imported.price,
    imported.unit,
    NULL,
    imported.image_url,
    TRUE,
    NOW(),
    NOW()
FROM attached_product_import imported
ON CONFLICT (slug) DO UPDATE
SET category_id = EXCLUDED.category_id,
    name = EXCLUDED.name,
    price = EXCLUDED.price,
    unit = EXCLUDED.unit,
    image_url = EXCLUDED.image_url,
    is_active = TRUE,
    updated_at = NOW();

DO $$
DECLARE
    imported_count INTEGER;
BEGIN
    SELECT COUNT(*)
    INTO imported_count
    FROM product existing
    JOIN attached_product_import imported ON imported.slug = existing.slug
    WHERE existing.category_id = imported.category_id;

    IF imported_count <> 20 THEN
        RAISE EXCEPTION 'Attached product import verification failed: expected 20 correctly categorized products, found %',
            imported_count;
    END IF;
END $$;

COMMIT;
