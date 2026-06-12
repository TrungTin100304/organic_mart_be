package com.bryan.migration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VinhomesGrandParkBuildingSeedTest {

    private static final String MIGRATION =
        "/db/migration/V18__seed_vinhomes_grand_park_buildings.sql";

    @Test
    void seedsAllRainbowBuildingsIdempotently() throws IOException {
        String sql;
        try (var stream = getClass().getResourceAsStream(MIGRATION)) {
            assertTrue(stream != null, "Vinhomes Grand Park seed migration must exist");
            sql = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }

        List<String> expectedCodes = List.of(
            "S1.01", "S1.02", "S1.03", "S1.05", "S1.06", "S1.07",
            "S2.01", "S2.02", "S2.03", "S2.05",
            "S3.01", "S3.02", "S3.03", "S3.05",
            "S5.01", "S5.02", "S5.03"
        );

        expectedCodes.forEach(code ->
            assertTrue(sql.contains("('" + code + "'"), "Missing building code " + code)
        );
        assertTrue(sql.contains("ON CONFLICT (code) DO UPDATE"), "Seed must be idempotent");
    }
}
