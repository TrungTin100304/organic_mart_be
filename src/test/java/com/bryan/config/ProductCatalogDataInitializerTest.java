package com.bryan.config;

import com.bryan.entity.ProductCategory;
import com.bryan.repository.ProductCategoryRepository;
import com.bryan.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductCatalogDataInitializerTest {

    @Test
    void createsMissingCatalogCategoriesBeforeNormalizingProducts() {
        ProductCategory vegetables = category("Vegetables", "rau-cu-qua", null, 10);
        ProductCategory fruits = category("Fruit", "trai-cay", null, 20);

        ProductRepository productRepository = mock(ProductRepository.class);
        ProductCategoryRepository categoryRepository = mock(ProductCategoryRepository.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

        when(categoryRepository.findAll()).thenReturn(List.of(vegetables, fruits));
        when(categoryRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(productRepository.findAll()).thenReturn(List.of());

        ProductCatalogDataInitializer initializer = new ProductCatalogDataInitializer(
                productRepository,
                categoryRepository,
                jdbcTemplate
        );

        assertDoesNotThrow(() -> initializer.run(null));

        @SuppressWarnings({"unchecked", "rawtypes"})
        ArgumentCaptor<Iterable<ProductCategory>> savedCategoriesCaptor = ArgumentCaptor.forClass((Class) Iterable.class);
        verify(categoryRepository).saveAll(savedCategoriesCaptor.capture());

        List<ProductCategory> savedCategories = StreamSupport
                .stream(savedCategoriesCaptor.getValue().spliterator(), false)
                .toList();

        assertSame(vegetables, category(savedCategories, "rau-an-la").getParent());
        assertSame(vegetables, category(savedCategories, "rau-an-cu").getParent());
        assertSame(vegetables, category(savedCategories, "rau-an-qua").getParent());
        assertSame(vegetables, category(savedCategories, "rau-gia-vi").getParent());
        assertSame(fruits, category(savedCategories, "trai-cay-trong-nuoc").getParent());
        assertNull(category(savedCategories, "nam").getParent());
        assertNull(category(savedCategories, "gia-vi-nguyen-vat-lieu-nau-an").getParent());
    }

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

    private static ProductCategory category(String name, String slug, ProductCategory parent, int sortOrder) {
        ProductCategory category = new ProductCategory();
        category.setName(name);
        category.setSlug(slug);
        category.setParent(parent);
        category.setSortOrder(sortOrder);
        return category;
    }

    private static ProductCategory category(List<ProductCategory> categories, String slug) {
        return categories.stream()
                .filter(category -> slug.equals(category.getSlug()))
                .findFirst()
                .orElseThrow();
    }
}
