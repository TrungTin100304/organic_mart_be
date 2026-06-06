BEGIN;

WITH child_categories (parent_slug, name, slug, sort_order) AS (
    VALUES
        ('rau-cu-qua', 'Rau lá', 'rau-la', 11),
        ('rau-cu-qua', 'Củ quả', 'cu-qua', 12),
        ('trai-cay', 'Trái cây tươi', 'trai-cay-tuoi', 21),
        ('sua-trung', 'Trứng hữu cơ', 'trung-huu-co', 31),
        ('ngu-coc-hat', 'Sữa hạt', 'sua-hat', 41),
        ('ngu-coc-hat', 'Ngũ cốc', 'ngu-coc', 42),
        ('thuc-pham-kho', 'Nấm khô', 'nam-kho', 51)
)
INSERT INTO product_category (name, slug, parent_id, sort_order, created_at)
SELECT child.name, child.slug, parent.id, child.sort_order, NOW()
FROM child_categories child
JOIN product_category parent ON parent.slug = child.parent_slug
ON CONFLICT (slug) DO UPDATE
SET name = EXCLUDED.name,
    parent_id = EXCLUDED.parent_id,
    sort_order = EXCLUDED.sort_order;

WITH product_category_updates (product_slug, category_slug) AS (
    VALUES
        ('rau-bo-xoi-organic-300g', 'rau-la'),
        ('cai-kale-organic-300g', 'rau-la'),
        ('ca-rot-da-lat-organic-500g', 'cu-qua'),
        ('ot-chuong-do-organic-300g', 'cu-qua'),
        ('tao-fuji-huu-co-500g', 'trai-cay-tuoi'),
        ('chuoi-cau-huu-co-1kg', 'trai-cay-tuoi'),
        ('trung-ga-tha-vuon-huu-co-10-qua', 'trung-huu-co'),
        ('sua-hat-hanh-nhan-huu-co-1l', 'sua-hat'),
        ('yen-mach-can-det-huu-co-500g', 'ngu-coc'),
        ('nam-huong-kho-huu-co-100g', 'nam-kho')
)
UPDATE product product
SET category_id = category.id,
    updated_at = NOW()
FROM product_category_updates update_rows
JOIN product_category category ON category.slug = update_rows.category_slug
WHERE product.slug = update_rows.product_slug;

COMMIT;
