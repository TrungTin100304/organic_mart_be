package com.bryan.migration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminFeatureMigrationTest {

    @Test
    void adminFeatureMigrationsUsePostgresqlSyntaxAndKeepStandardDeliveryFree() throws IOException {
        for (String migration : List.of(
                "/db/migration/V20__reviews.sql",
                "/db/migration/V21__delivery_settings.sql",
                "/db/migration/V22__audit_logs.sql")) {
            String sql = read(migration);
            assertFalse(sql.contains("AUTO_INCREMENT"), migration + " must use PostgreSQL identity syntax");
            assertFalse(sql.contains("DATETIME"), migration + " must use PostgreSQL timestamp syntax");
            assertTrue(sql.contains("BIGSERIAL"), migration + " must use BIGSERIAL primary keys");
        }

        String deliverySettings = read("/db/migration/V21__delivery_settings.sql");
        assertTrue(deliverySettings.contains("('STANDARD', 0, 60"),
                "Standard internal delivery must remain free by default");
        assertTrue(deliverySettings.contains("ON CONFLICT (delivery_type) DO NOTHING"),
                "Delivery setting seed must be idempotent");
    }

    private String read(String path) throws IOException {
        try (var stream = getClass().getResourceAsStream(path)) {
            assertTrue(stream != null, path + " must exist");
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
