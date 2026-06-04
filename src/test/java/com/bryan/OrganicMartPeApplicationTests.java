package com.bryan;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = "spring.flyway.enabled=false")
class OrganicMartPeApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void checkDatabase() {
        jdbcTemplate.query("SELECT slug, is_active FROM product", rs -> {
            System.out.println("DB_PRODUCT_VAL: " + rs.getString("slug") + " -> " + rs.getBoolean("is_active"));
        });
    }

}
