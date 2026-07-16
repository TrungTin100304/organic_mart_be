package com.bryan.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RenderDatabaseUrlSupport {

    private static final String DATABASE_URL = "DATABASE_URL";
    private static final String SPRING_DATASOURCE_URL = "SPRING_DATASOURCE_URL";
    private static final String SPRING_DATASOURCE_USERNAME = "SPRING_DATASOURCE_USERNAME";
    private static final String SPRING_DATASOURCE_PASSWORD = "SPRING_DATASOURCE_PASSWORD";
    private static final String SPRING_DATASOURCE_DRIVER_CLASS_NAME = "SPRING_DATASOURCE_DRIVER_CLASS_NAME";

    private RenderDatabaseUrlSupport() {
    }

    public static void applyToSystemProperties() {
        if (hasText(System.getProperty(SPRING_DATASOURCE_URL))
                || hasText(System.getenv(SPRING_DATASOURCE_URL))) {
            return;
        }

        Map<String, String> datasourceProperties = toSpringDatasourceProperties(System.getenv(DATABASE_URL));
        datasourceProperties.forEach((key, value) -> {
            if (!hasText(System.getProperty(key)) && !hasText(System.getenv(key))) {
                System.setProperty(key, value);
            }
        });
    }

    static Map<String, String> toSpringDatasourceProperties(String databaseUrl) {
        if (!hasText(databaseUrl)) {
            return Map.of();
        }

        URI uri = URI.create(databaseUrl);
        String scheme = uri.getScheme();
        if (!"postgres".equalsIgnoreCase(scheme) && !"postgresql".equalsIgnoreCase(scheme)) {
            return Map.of();
        }

        String host = uri.getHost();
        String path = uri.getRawPath();
        if (!hasText(host) || !hasText(path) || "/".equals(path)) {
            throw new IllegalArgumentException("DATABASE_URL must include a host and database name.");
        }

        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://").append(host);
        if (uri.getPort() != -1) {
            jdbcUrl.append(':').append(uri.getPort());
        }
        jdbcUrl.append(path);
        if (hasText(uri.getRawQuery())) {
            jdbcUrl.append('?').append(uri.getRawQuery());
        }

        Map<String, String> properties = new LinkedHashMap<>();
        properties.put(SPRING_DATASOURCE_URL, jdbcUrl.toString());

        String rawUserInfo = uri.getRawUserInfo();
        if (hasText(rawUserInfo)) {
            String[] credentials = rawUserInfo.split(":", 2);
            properties.put(SPRING_DATASOURCE_USERNAME, decode(credentials[0]));
            if (credentials.length > 1) {
                properties.put(SPRING_DATASOURCE_PASSWORD, decode(credentials[1]));
            }
        }

        properties.put(SPRING_DATASOURCE_DRIVER_CLASS_NAME, "org.postgresql.Driver");
        return properties;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
