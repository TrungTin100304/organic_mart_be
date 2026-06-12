package com.bryan.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductCatalogDataInitializerTest {

    @Test
    void mapsEveryKnownCatalogProductToALeafCategory() {
        var categoryBySlug = ProductCatalogDataInitializer.categoryByProductSlug();

        assertEquals(82, categoryBySlug.size());
        assertEquals("rau-an-la", categoryBySlug.get("rau-ma-organic-300gr"));
        assertEquals("rau-an-cu", categoryBySlug.get("khoai-lang-nhat-organic-300gr"));
        assertEquals("rau-an-qua", categoryBySlug.get("dau-bap-organic-300gr"));
        assertEquals("trai-cay-trong-nuoc", categoryBySlug.get("tac-quat-organic-300gr"));
        assertEquals("gia-vi-nguyen-vat-lieu-nau-an", categoryBySlug.get("trung-ga-ta-sach-10-qua"));
        assertEquals("rau-an-cu", categoryBySlug.get("khoai-mon-sap-vang-500gr"));
        assertEquals("rau-an-qua", categoryBySlug.get("dau-bap-250gr"));
        assertEquals("trai-cay-trong-nuoc", categoryBySlug.get("tac-quat-200gr"));
        assertEquals("gia-vi-nguyen-vat-lieu-nau-an", categoryBySlug.get("trung-ga-ta"));
    }

    @Test
    void containsEveryProductFromTheAttachedCatalog() {
        assertEquals(20, ProductCatalogDataInitializer.attachedProducts().size());
    }

    @Test
    void infersSellingUnitsFromProductNames() {
        assertEquals("300gr", ProductCatalogDataInitializer.inferUnit("Rau má Organic 300gr", "rau-ma-organic-300gr"));
        assertEquals("10 quả", ProductCatalogDataInitializer.inferUnit("Trứng gà ta sạch - 10 quả", "trung-ga-ta-sach-10-qua"));
        assertEquals("10 quả", ProductCatalogDataInitializer.inferUnit("Trứng gà ta sạch - 10 quả", "trung-ga-ta"));
        assertEquals("hộp", ProductCatalogDataInitializer.inferUnit("Đậu Hũ NON GMO", "dau-hu-non-gmo"));
    }
}
