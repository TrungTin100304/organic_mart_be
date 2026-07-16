package com.bryan.migration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttachedProductsImportMigrationTest {

    private static final String MIGRATION = "/db/migration/V30__import_attached_products.sql";

    @Test
    void importsEveryAttachedProductIntoTheExpectedLeafCategory() throws IOException {
        String sql = read(MIGRATION);

        Map<String, Integer> expectedCategoryBySlug = Map.ofEntries(
            Map.entry("trung-ga-ta-sach-10-qua", 4),
            Map.entry("tac-quat-organic-300gr", 12),
            Map.entry("khoai-lang-nhat-organic-300gr", 7),
            Map.entry("dau-bap-organic-300gr", 8),
            Map.entry("bap-chuoi-organic-1kg", 6),
            Map.entry("cai-tatsoi-cai-hoa-hong-organic-250gr", 6),
            Map.entry("xa-lach-mo-organic-300gr", 6),
            Map.entry("xa-lach-lo-lo-organic-300gr", 6),
            Map.entry("dua-leo-organic-300gr", 8),
            Map.entry("ca-tim-organic-300gr", 8),
            Map.entry("rau-ma-organic-300gr", 6),
            Map.entry("bap-nep-organic-300gr", 8),
            Map.entry("khoai-mon-sap-vang-organic-300gr", 7),
            Map.entry("khoai-mo-organic-300gr", 7),
            Map.entry("cu-cai-do-300gr", 7),
            Map.entry("cu-cai-trang-organic-300gr", 7),
            Map.entry("cu-hoi-organic-500gr", 7),
            Map.entry("cu-den-organic-300gr", 7),
            Map.entry("su-hao-trang-organic-300g", 7),
            Map.entry("khoai-lang-mat-300gr", 7)
        );

        expectedCategoryBySlug.forEach((slug, categoryId) ->
            assertTrue(
                sql.contains("('" + slug + "',") && sql.contains("'" + slug + "',") && containsCategoryRow(sql, slug, categoryId),
                slug + " must map to category " + categoryId
            )
        );

        long productRows = sql.lines()
            .map(String::trim)
            .filter(line -> line.matches("\\('[^']+', '[^']+', \\d+, \\d+(\\.\\d+)?, '[^']+', 'https?://[^']+'\\)[,;]?"))
            .count();

        assertEquals(20, productRows);
        assertTrue(sql.contains("id = 7 AND parent_id = 1 AND slug = 'rau-an-cu'"));
        assertTrue(sql.contains("id = 6 AND parent_id = 1 AND slug = 'rau-an-la'"));
        assertTrue(sql.contains("id = 8 AND parent_id = 1 AND slug = 'rau-an-qua'"));
        assertTrue(sql.contains("id = 12 AND parent_id = 2 AND slug = 'trai-cay-trong-nuoc'"));
        assertFalse(sql.lines().anyMatch(line -> line.trim().matches("\\('[^']+', '[^']+', 1,.*")));
        assertTrue(sql.contains("Attached product name collision:"));
        assertTrue(sql.contains("ON CONFLICT (slug) DO UPDATE"));
    }

    private boolean containsCategoryRow(String sql, String slug, int categoryId) {
        return sql.lines()
            .map(String::trim)
            .anyMatch(line -> line.startsWith("('" + slug + "',") && line.contains("', " + categoryId + ","));
    }

    private String read(String path) throws IOException {
        try (var stream = getClass().getResourceAsStream(path)) {
            assertTrue(stream != null, path + " must exist");
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
