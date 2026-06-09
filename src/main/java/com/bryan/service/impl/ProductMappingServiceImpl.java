package com.bryan.service.impl;

import com.bryan.dto.response.MealProductResponse;
import com.bryan.entity.Product;
import com.bryan.repository.ProductRepository;
import com.bryan.service.ProductMappingService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductMappingServiceImpl implements ProductMappingService {

    private final ProductRepository productRepository;

    private List<Product> cachedProducts = Collections.emptyList();
    private volatile long lastCacheTime = 0;
    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes
    private static final double SIMILARITY_THRESHOLD = 0.35;

    @PostConstruct
    public void init() {
        refreshCache();
    }

    @Override
    public Optional<MealProductResponse> findMatchingProduct(String ingredientName) {
        refreshCacheIfNeeded();
        return findBestMatch(ingredientName, cachedProducts);
    }

    @Override
    public List<MealProductResponse> mapIngredients(List<String> ingredientNames) {
        List<MealProductResponse> results = new ArrayList<>();
        for (String ingredient : ingredientNames) {
            if (ingredient == null || ingredient.isBlank()) continue;
            Optional<MealProductResponse> matched = findMatchingProduct(ingredient);
            if (matched.isPresent()) {
                results.add(matched.get());
            }
        }
        return results;
    }

    private void refreshCacheIfNeeded() {
        if (System.currentTimeMillis() - lastCacheTime > CACHE_TTL_MS) {
            refreshCache();
        }
    }

    private synchronized void refreshCache() {
        try {
            List<Product> products = productRepository.findAllActiveWithInventory();
            cachedProducts = products;
            lastCacheTime = System.currentTimeMillis();
            log.debug("Refreshed product cache with {} active products", products.size());
        } catch (Exception ex) {
            log.warn("Failed to refresh product cache: {}", ex.getMessage());
        }
    }

    private Optional<MealProductResponse> findBestMatch(String ingredientName, List<Product> products) {
        String normalized = normalize(ingredientName);
        if (normalized.isBlank()) return Optional.empty();

        Product bestMatch = null;
        double bestScore = 0;

        for (Product product : products) {
            double score = calculateSimilarity(normalized, product);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = product;
            }
        }

        if (bestMatch == null || bestScore < SIMILARITY_THRESHOLD) {
            return Optional.empty();
        }

        boolean inStock = hasStock(bestMatch);

        MealProductResponse response = new MealProductResponse(
                null,
                bestMatch.getId(),
                bestMatch.getName(),
                bestMatch.getPrice(),
                bestMatch.getImageUrl(),
                bestMatch.getUnit(),
                ingredientName,
                null, // quantity set by caller
                bestMatch.getUnit(),
                bestMatch.getPrice(),
                inStock,
                false
        );

        return Optional.of(response);
    }

    private double calculateSimilarity(String normalizedIngredient, Product product) {
        String normalizedName = normalize(product.getName());
        String normalizedCategory = product.getCategory() != null
                ? normalize(product.getCategory().getName())
                : "";

        double nameScore = jaroWinklerSimilarity(normalizedIngredient, normalizedName);

        double categoryScore = normalizedCategory.isBlank()
                ? 0
                : jaroWinklerSimilarity(normalizedIngredient, normalizedCategory) * 0.6;

        return Math.max(nameScore, categoryScore);
    }

    private boolean hasStock(Product product) {
        if (product.getInventoryBatches() == null || product.getInventoryBatches().isEmpty()) {
            return false;
        }
        return product.getInventoryBatches().stream()
                .filter(batch -> batch.getExpiryDate() != null &&
                        !batch.getExpiryDate().isBefore(java.time.LocalDate.now()))
                .mapToDouble(batch -> batch.getQuantityRemaining().doubleValue())
                .sum() > 0;
    }

    private String normalize(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text.toLowerCase().trim(), Normalizer.Form.NFD);
        normalized = diacriticalCharsPattern.matcher(normalized).replaceAll("");
        normalized = normalized.replaceAll("[^a-z0-9\\s]", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }

    private static final Pattern diacriticalCharsPattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private double jaroWinklerSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0;
        if (s1.equals(s2)) return 1.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0;

        int len1 = s1.length();
        int len2 = s2.length();
        int matchWindow = Math.max(len1, len2) / 2 - 1;
        if (matchWindow < 0) matchWindow = 0;

        boolean[] s1Matches = new boolean[len1];
        boolean[] s2Matches = new boolean[len2];
        Arrays.fill(s1Matches, false);
        Arrays.fill(s2Matches, false);

        int matches = 0;
        int transpositions = 0;

        for (int i = 0; i < len1; i++) {
            int start = Math.max(0, i - matchWindow);
            int end = Math.min(i + matchWindow + 1, len2);
            for (int j = start; j < end; j++) {
                if (s2Matches[j] || s1.charAt(i) != s2.charAt(j)) continue;
                s1Matches[i] = true;
                s2Matches[j] = true;
                matches++;
                break;
            }
        }

        if (matches == 0) return 0;

        int k = 0;
        for (int i = 0; i < len1; i++) {
            if (!s1Matches[i]) continue;
            while (!s2Matches[k]) k++;
            if (s1.charAt(i) != s2.charAt(k)) transpositions++;
            k++;
        }

        double jaro = (matches / (double) len1 + matches / (double) len2 + (matches - transpositions / 2.0) / matches) / 3.0;

        int prefix = 0;
        for (int i = 0; i < Math.min(4, Math.min(len1, len2)); i++) {
            if (s1.charAt(i) == s2.charAt(i)) prefix++;
            else break;
        }

        return jaro + prefix * 0.1 * (1 - jaro);
    }
}
