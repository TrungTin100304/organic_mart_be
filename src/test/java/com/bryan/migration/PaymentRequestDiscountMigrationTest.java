package com.bryan.migration;

import com.bryan.entity.PaymentRequest;
import org.hibernate.annotations.ColumnDefault;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentRequestDiscountMigrationTest {

    @Test
    void discountAmountHasDatabaseDefaultForHibernateSchemaUpdate() throws NoSuchFieldException {
        ColumnDefault columnDefault = PaymentRequest.class
                .getDeclaredField("discountAmount")
                .getAnnotation(ColumnDefault.class);

        assertTrue(columnDefault != null, "discountAmount must declare a database default");
        assertEquals("0", columnDefault.value());
        assertEquals(BigDecimal.ZERO, new PaymentRequest().getDiscountAmount());
    }

    @Test
    void repairMigrationBackfillsNullsBeforeAddingNotNullConstraint() throws IOException {
        String sql = read("/db/migration/V26__repair_payment_request_discount_amount.sql");

        int updateNulls = sql.indexOf("UPDATE payment_request");
        int setDefault = sql.indexOf("SET DEFAULT 0");
        int setNotNull = sql.indexOf("SET NOT NULL");

        assertTrue(updateNulls >= 0, "migration must backfill existing rows");
        assertTrue(setDefault > updateNulls, "migration must set a database default after backfill");
        assertTrue(setNotNull > setDefault, "migration must add NOT NULL only after backfill");
    }

    private String read(String path) throws IOException {
        try (var stream = getClass().getResourceAsStream(path)) {
            assertTrue(stream != null, path + " must exist");
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
