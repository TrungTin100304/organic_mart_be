package com.bryan.config;

import com.bryan.entity.Product;
import com.bryan.entity.ProductCategory;
import com.bryan.repository.ProductCategoryRepository;
import com.bryan.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCatalogDataInitializer implements ApplicationRunner {

    private static final Pattern SELLING_UNIT = Pattern.compile("(\\d+)\\s*(gr|g|kg|ml|l)\\b", Pattern.CASE_INSENSITIVE);
    private static final String DEFAULT_DESCRIPTION = "Sản phẩm hữu cơ tươi sạch từ Organic Mart.";

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, String> categoryMappings = categoryByProductSlug();
        Map<String, ProductCategory> categories = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(ProductCategory::getSlug, Function.identity()));
        validateCategories(categoryMappings, categories);

        Map<String, Product> productsBySlug = productRepository.findAll().stream()
                .filter(product -> !isBlank(product.getSlug()))
                .collect(Collectors.toMap(Product::getSlug, Function.identity()));

        for (AttachedProductSeed seed : attachedProducts()) {
            Product product = productsBySlug.computeIfAbsent(seed.slug(), ignored -> new Product());
            product.setSlug(seed.slug());
            product.setName(seed.name());
            product.setPrice(BigDecimal.valueOf(seed.price()));
            product.setUnit(seed.unit());
            product.setImageUrl(seed.imageUrl());
            product.setActive(true);
            product.setCategory(categories.get(seed.categorySlug()));
            if (isBlank(product.getDescription())) {
                product.setDescription(DEFAULT_DESCRIPTION);
            }
        }

        for (Map.Entry<String, String> mapping : categoryMappings.entrySet()) {
            Product product = productsBySlug.get(mapping.getKey());
            if (product == null) {
                continue;
            }
            product.setCategory(categories.get(mapping.getValue()));
            product.setUnit(inferUnit(product.getName(), product.getSlug()));
        }

        List<Product> normalizedProducts = categoryMappings.keySet().stream()
                .map(productsBySlug::get)
                .filter(Objects::nonNull)
                .toList();
        productRepository.saveAll(normalizedProducts);
        productRepository.flush();
        int deletedInvalidRows = deleteUnreferencedBlankProducts();

        log.info(
                "Normalized product catalog: {} known products, {} attached products, {} invalid rows removed",
                normalizedProducts.size(),
                attachedProducts().size(),
                deletedInvalidRows
        );
    }

    static Map<String, String> categoryByProductSlug() {
        Map<String, String> mappings = new LinkedHashMap<>();

        assign(mappings, "rau-an-la",
                "cai-kale-xoan-organic-300gr gia-sach-u-cat-300gr cai-bo-xoi-organic-300gr "
                        + "rau-can-tay-organic-300gr tan-o-300gr rau-den-organic-300gr rau-mong-toi-organic-300gr "
                        + "cai-thao-dun-organic-300gr cai-xanh-organic-300gr cai-ngot-300gr cai-ngong-organic-300gr "
                        + "cai-thia-organic-300gr bong-cai-xanh-baby-300gr bong-cai-xanh-organic-300g "
                        + "bap-cai-tim-organic-300g rau-ngot-organic-300g rau-muong-organic-300gr "
                        + "ngon-su-su-organic-300g la-bo-cong-anh-organic-300g xa-lach-mo-organic-300gr "
                        + "xa-lach-lo-lo-organic-300gr cai-tatsoi-cai-hoa-hong-organic-250gr "
                        + "bap-chuoi-organic-1kg rau-ma-organic-300gr");

        assign(mappings, "rau-an-cu",
                "khoai-mo-organic-300gr khoai-mon-sap-vang-organic-300gr cu-cai-do-300gr "
                        + "cu-cai-trang-organic-300gr cu-hoi-organic-500gr cu-den-organic-300gr "
                        + "ca-rot-organic-300g su-hao-trang-organic-300g khoai-lang-mat-300gr "
                        + "hanh-tay-organic-300g khoai-lang-nhat-organic-300gr");

        assign(mappings, "rau-an-qua",
                "ot-chuong-do-organic-300gr muop-organic-300gr kho-qua-organic-300gr "
                        + "dau-cove-dau-que-organic-300gr bau-sao-organic-300gr ca-chua-rita-organic-300g "
                        + "ca-chua-beef-organic-300g bi-dao-organic-300g bi-do-organic-300g "
                        + "ot-chuong-xanh-organic-300gr ot-chuong-vang-organic-300gr bi-ngoi-organic-300gr "
                        + "bap-nep-organic-300gr ca-tim-organic-300gr dua-leo-organic-300gr dau-bap-organic-300gr");

        assign(mappings, "rau-gia-vi",
                "hanh-tim-kho-organic-300gr toi-tia-nghe-an-organic-000gr sa-cay-organic-300g");

        assign(mappings, "nam",
                "nam-huong-tuoi-green-kingdom-150gr nam-mo-green-kingdom-150gr "
                        + "nam-huong-kho-green-kingdom-50gr nam-dui-ga-green-kingdom-150gr");

        assign(mappings, "trai-cay-trong-nuoc", "chanh-organic-300gr tac-quat-organic-300gr");
        assign(mappings, "gia-vi-nguyen-vat-lieu-nau-an", "dau-hu-non-gmo trung-ga-ta-sach-10-qua");

        attachedProducts().forEach(seed -> mappings.put(seed.slug(), seed.categorySlug()));

        return Map.copyOf(mappings);
    }

    static List<AttachedProductSeed> attachedProducts() {
        return List.of(
                seed("trung-ga-ta", "Trứng gà ta sạch - 10 quả", "gia-vi-nguyen-vat-lieu-nau-an", 75000, "10 quả", "https://bizweb.dktcdn.net/100/390/808/products/7dc26e6782aedbf8f16f9d8d0e316f87.jpg?v=1604733176453"),
                seed("tac-quat-200gr", "Tắc (Quất) organic 300gr", "trai-cay-trong-nuoc", 30000, "300gr", "https://bizweb.dktcdn.net/100/390/808/products/trai-tac-1kg.jpg?v=1600505273100"),
                seed("khoai-lang-nhat-1kg", "Khoai Lang Nhật Organic - 300gr", "rau-an-cu", 27000, "300gr", "https://bizweb.dktcdn.net/100/390/808/products/khoai-lang-huu-co-500x500.jpg?v=1600505034837"),
                seed("dau-bap-250gr", "Đậu Bắp Organic 300gr", "rau-an-qua", 27000, "300gr", "https://bizweb.dktcdn.net/100/390/808/products/dau-bap-huu-co-500x500.jpg?v=1600504946570"),
                seed("bap-chuoi-1kg", "Bắp Chuối Organic 1kg", "rau-an-la", 45000, "1kg", "https://bizweb.dktcdn.net/100/390/808/products/hoa-chuoi-huu-co-500x500.jpg?v=1600504848320"),
                seed("cai-tatsoi-cai-hoa-hong-250gr", "Cải Tatsoi (Cải Hoa Hồng) Organic 250gr", "rau-an-la", 27000, "250gr", "https://bizweb.dktcdn.net/100/390/808/products/58d96e4f4ac2d512db50144d4d677c034301b086.jpg?v=1600504762873"),
                seed("xa-lach-mo-250gr", "Xà Lách Mỡ Organic 300gr", "rau-an-la", 30000, "300gr", "https://bizweb.dktcdn.net/100/390/808/products/2003d66491a1c8fc3e629034b0288a74.jpg?v=1600504580920"),
                seed("xa-lach-lo-lo-250gr", "Xà Lách Lo Lo Organic 300gr", "rau-an-la", 30000, "300gr", "https://bizweb.dktcdn.net/100/390/808/products/xa-lach-lolo-xanh-500x500.jpg?v=1600504442550"),
                seed("dua-leo-500gr", "Dưa Leo Organic 300gr", "rau-an-qua", 27000, "300gr", "https://bizweb.dktcdn.net/100/390/808/products/16429764214814.jpg?v=1600504354567"),
                seed("ca-tim-250gr", "Cà Tím Organic 300gr", "rau-an-qua", 26000, "300gr", "https://bizweb.dktcdn.net/100/390/808/products/20190405174004hat-giong-ca-tim.png?v=1600504262433"),
                seed("rau-ma-250gr", "Rau má Organic 300gr", "rau-an-la", 35000, "300gr", "https://bizweb.dktcdn.net/100/390/808/products/nu-c-p-rau-m-min.jpg?v=1600504031593"),
                seed("bap-nep-500gr", "Bắp Nếp Organic 300gr", "rau-an-qua", 23000, "300gr", "https://bizweb.dktcdn.net/100/390/808/products/hat-giong-bap-nep-nu-1-2.jpg?v=1600503780137"),
                seed("khoai-mon-sap-vang-500gr", "Khoai môn sáp vàng Organic 300gr", "rau-an-cu", 26000, "300gr", "https://bizweb.dktcdn.net/100/390/808/products/faf6b495f2c24260b79acdd95099008a-4a337a83364b43a59be3cb4b815496fd-master.jpg?v=1593163116783"),
                seed("khoai-mo-500gr", "Khoai mỡ Organic 300gr", "rau-an-cu", 27000, "300gr", "https://bizweb.dktcdn.net/100/390/808/products/13405198-jpeg.jpg?v=1593163339683"),
                seed("cu-cai-do-500gr", "Củ cải đỏ 300gr", "rau-an-cu", 24000, "300gr", "https://bizweb.dktcdn.net/100/390/808/products/cu-cai-duong-huu-co-500x500.jpg?v=1592813122220"),
                seed("cu-cai-trang-500gr", "Củ cải trắng Organic 300gr", "rau-an-cu", 30000, "300gr", "https://bizweb.dktcdn.net/100/390/808/products/847184545894.jpg?v=1592812267877"),
                seed("cu-hoi-500gr", "Củ hồi Organic 500gr", "rau-an-cu", 47500, "500gr", "https://bizweb.dktcdn.net/100/390/808/products/upload-9bdbd4d6855d438fae803c3fbf56e5dd-large.jpg?v=1592810724327"),
                seed("cu-den-500gr", "Củ dền Organic 300gr", "rau-an-cu", 30000, "300gr", "https://bizweb.dktcdn.net/100/390/808/products/cu-den-do-baby-large.jpg?v=1593855273587"),
                seed("su-hao-trang-organic-500g", "Su hào trắng Organic 300g", "rau-an-cu", 27000, "300g", "https://bizweb.dktcdn.net/100/390/808/products/product2263.jpg?v=1592366623437"),
                seed("khoai-lang-mat-1kg", "Khoai lang mật - 300gr", "rau-an-cu", 19000, "300gr", "https://bizweb.dktcdn.net/100/390/808/products/77158-nguyen-lieu-lam-mut-khoai-lang-kho-gion-700x704.jpg?v=1592366775133")
        );
    }

    static String inferUnit(String name, String slug) {
        if ("trung-ga-ta-sach-10-qua".equals(slug) || "trung-ga-ta".equals(slug)) {
            return "10 quả";
        }
        if ("dau-hu-non-gmo".equals(slug)) {
            return "hộp";
        }
        if ("toi-tia-nghe-an-organic-000gr".equals(slug)) {
            return "300gr";
        }

        Matcher matcher = SELLING_UNIT.matcher(name == null ? "" : name);
        String unit = null;
        while (matcher.find()) {
            unit = matcher.group(1) + matcher.group(2).toLowerCase();
        }
        return unit != null ? unit : "sản phẩm";
    }

    private static void assign(Map<String, String> mappings, String categorySlug, String productSlugs) {
        for (String productSlug : productSlugs.split(" ")) {
            mappings.put(productSlug, categorySlug);
        }
    }

    private static AttachedProductSeed seed(
            String slug,
            String name,
            String categorySlug,
            long price,
            String unit,
            String imageUrl
    ) {
        return new AttachedProductSeed(slug, name, categorySlug, price, unit, imageUrl);
    }

    private void validateCategories(
            Map<String, String> categoryMappings,
            Map<String, ProductCategory> categories
    ) {
        List<String> missing = categoryMappings.values().stream()
                .distinct()
                .filter(slug -> !categories.containsKey(slug))
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing product categories: " + missing);
        }
    }

    private int deleteUnreferencedBlankProducts() {
        return jdbcTemplate.update("""
                DELETE FROM product product
                WHERE (TRIM(COALESCE(product.name, '')) = '' OR TRIM(COALESCE(product.slug, '')) = '')
                  AND NOT EXISTS (SELECT 1 FROM cart_item item WHERE item.product_id = product.id)
                  AND NOT EXISTS (SELECT 1 FROM inventory_batch batch WHERE batch.product_id = product.id)
                  AND NOT EXISTS (SELECT 1 FROM order_detail detail WHERE detail.product_id = product.id)
                  AND NOT EXISTS (SELECT 1 FROM reviews review WHERE review.product_id = product.id)
                  AND NOT EXISTS (SELECT 1 FROM product_allergen allergen WHERE allergen.product_id = product.id)
                """);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    record AttachedProductSeed(
            String slug,
            String name,
            String categorySlug,
            long price,
            String unit,
            String imageUrl
    ) {
    }
}
