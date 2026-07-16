package com.bryan.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class RenderDatabaseUrlSupportTest {

    @Test
    void convertsRenderPostgresUrlToSpringDatasourceProperties() {
        Map<String, String> properties = RenderDatabaseUrlSupport.toSpringDatasourceProperties(
                "postgresql://organic_user:pa%24%24word@dpg-example-a.singapore-postgres.render.com:5432/organic_mart?sslmode=require");

        assertThat(properties)
                .containsEntry(
                        "SPRING_DATASOURCE_URL",
                        "jdbc:postgresql://dpg-example-a.singapore-postgres.render.com:5432/organic_mart?sslmode=require")
                .containsEntry("SPRING_DATASOURCE_USERNAME", "organic_user")
                .containsEntry("SPRING_DATASOURCE_PASSWORD", "pa$$word")
                .containsEntry("SPRING_DATASOURCE_DRIVER_CLASS_NAME", "org.postgresql.Driver");
    }

    @Test
    void ignoresMissingDatabaseUrl() {
        assertThat(RenderDatabaseUrlSupport.toSpringDatasourceProperties("")).isEmpty();
        assertThat(RenderDatabaseUrlSupport.toSpringDatasourceProperties(null)).isEmpty();
    }
}
