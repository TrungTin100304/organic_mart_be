package com.bryan.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecurityConfigCorsTest {

    @Test
    void parsesConfiguredAllowedOriginPatterns() {
        assertEquals(
                List.of("http://localhost:*", "https://shop.example.com"),
                SecurityConfig.parseAllowedOriginPatterns(
                        " http://localhost:* , https://shop.example.com "
                )
        );
    }

    @Test
    void fallsBackToSafeDefaultPatternsWhenConfigIsBlank() {
        assertEquals(
                List.of("http://localhost:*", "http://127.0.0.1:*", "https://*.vercel.app"),
                SecurityConfig.parseAllowedOriginPatterns(" ")
        );
    }
}
