package com.bryan.repository;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class AdminRepositoryQuerySafetyTest {

    @Test
    void adminSearchRepositoriesMustNotUseNullableJpqlParameters() throws IOException {
        for (String repository : List.of(
                "UserRepository.java",
                "OrderRepository.java",
                "PaymentRequestRepository.java",
                "ReviewRepository.java",
                "SepayWebhookEventRepository.java")) {
            String source = Files.readString(Path.of(
                    "src", "main", "java", "com", "bryan", "repository", repository));

            assertFalse(source.matches("(?s).*:\\w+\\s+IS\\s+NULL.*"),
                    repository + " must use dynamic Specifications instead of nullable JPQL parameters");
        }
    }
}
