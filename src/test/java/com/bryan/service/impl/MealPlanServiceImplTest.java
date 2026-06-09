package com.bryan.service.impl;

import com.bryan.config.MealPlanProperties;
import com.bryan.dto.request.MealPlanGenerationRequest;
import com.bryan.dto.response.MealProductResponse;
import com.bryan.dto.response.MealPlanResponse;
import com.bryan.entity.*;
import com.bryan.exception.BadRequestException;
import com.bryan.exception.MealPlanRateLimitException;
import com.bryan.repository.*;
import com.bryan.service.GeminiAiService;
import com.bryan.service.MealPlanRateLimitService;
import com.bryan.service.ProductMappingService;
import com.bryan.dto.request.AiMealResponse;
import com.bryan.dto.request.AiDayPlan;
import com.bryan.dto.request.AiMeal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealPlanServiceImplTest {

    @Mock private MealPlanRepository mealPlanRepository;
    @Mock private MealRepository mealRepository;
    @Mock private MealProductRepository mealProductRepository;
    @Mock private CartRepository cartRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserPreferenceRepository userPreferenceRepository;
    @Mock private AllergenRepository allergenRepository;
    @Mock private GeminiAiService geminiAiService;
    @Mock private ProductMappingService productMappingService;
    @Mock private MealPlanRateLimitService rateLimitService;

    @InjectMocks
    private MealPlanServiceImpl mealPlanService;

    private User testUser;
    private MealPlanGenerationRequest validRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test User");
        testUser.setEmail("test@test.com");
        testUser.setAllergens(new HashSet<>());

        validRequest = new MealPlanGenerationRequest(
                3, 3, 1, "NORMAL",
                1500, BigDecimal.valueOf(200000), 60,
                List.of("bông cải xanh", "ức gà"),
                List.of("tôm"),
                "Ưa ăn nhẹ"
        );
    }

    // ─── Rate Limit Tests ────────────────────────────────────────────────────────

    @Test
    void shouldRejectRequestWhenRateLimitExceeded() {
        doThrow(new MealPlanRateLimitException("Quá nhiều yêu cầu"))
                .when(rateLimitService).checkRateLimit(1L);

        assertThrows(MealPlanRateLimitException.class, () ->
                mealPlanService.generateMealPlan(1L, validRequest));

        verify(mealPlanRepository, never()).save(any());
    }

    // ─── Validation Tests ────────────────────────────────────────────────────────

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () ->
                mealPlanService.generateMealPlan(999L, validRequest));
    }

    // ─── Ownership Tests ─────────────────────────────────────────────────────────

    @Test
    void shouldThrowWhenDeletingPlanNotOwnedByUser() {
        when(mealPlanRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () ->
                mealPlanService.deleteMealPlan(1L, 2L));

        verify(mealPlanRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenGettingPlanNotOwnedByUser() {
        when(mealPlanRepository.findByIdAndUserIdWithDetails(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () ->
                mealPlanService.getMealPlanById(1L, 2L));
    }

    @Test
    void shouldDeleteOwnedPlan() {
        MealPlan plan = new MealPlan();
        plan.setId(1L);
        plan.setUser(testUser);
        when(mealPlanRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(plan));

        mealPlanService.deleteMealPlan(1L, 1L);

        verify(mealPlanRepository).delete(plan);
    }

    // ─── Allergen Blocking Tests ─────────────────────────────────────────────────

    @Test
    void shouldPassAllergensToAi() throws Exception {
        // User is allergic to peanut
        Allergen peanut = new Allergen();
        peanut.setId(1L);
        peanut.setName("Peanut");
        testUser.getAllergens().add(peanut);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userPreferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());

        AiMealResponse aiResponse = new AiMealResponse();
        aiResponse.days = List.of(
                createAiDay(1, List.of(
                        createAiMeal("BREAKFAST", "Salad rau bina",
                                List.of("bông cải xanh"))
                ))
        );

        when(geminiAiService.generateMealPlan(any())).thenReturn(aiResponse);
        when(productMappingService.mapIngredients(anyList())).thenReturn(List.of());

        MealPlan savedPlan = new MealPlan();
        savedPlan.setId(1L);
        savedPlan.setUser(testUser);
        savedPlan.setName("Thực đơn " + LocalDate.now());
        savedPlan.setNumberOfDays(3);
        savedPlan.setMealsPerDay(3);
        savedPlan.setServings(1);
        savedPlan.setDietType("NORMAL");
        savedPlan.setStatus(MealPlanStatus.COMPLETED);
        savedPlan.setMeals(new ArrayList<>());

        when(mealPlanRepository.save(any(MealPlan.class))).thenReturn(savedPlan);

        MealPlanResponse response = mealPlanService.generateMealPlan(1L, validRequest);

        assertNotNull(response);
        // Verify allergen info was passed to AI
        ArgumentCaptor<com.bryan.dto.request.AiMealRequest> captor =
                ArgumentCaptor.forClass(com.bryan.dto.request.AiMealRequest.class);
        verify(geminiAiService).generateMealPlan(captor.capture());
        assertTrue(captor.getValue().userAllergenNames().contains("peanut"));
    }

    // ─── Product Mapping Tests ──────────────────────────────────────────────────

    @Test
    void shouldMapIngredientsToActiveProducts() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userPreferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());

        AiMealResponse aiResponse = new AiMealResponse();
        aiResponse.days = List.of(
                createAiDay(1, List.of(
                        createAiMeal("BREAKFAST", "Salad rau bina",
                                List.of("bông cải xanh", "dầu ô liu"))
                ))
        );

        when(geminiAiService.generateMealPlan(any())).thenReturn(aiResponse);

        // Product found
        when(productMappingService.mapIngredients(anyList())).thenReturn(List.of(
                new MealProductResponse(1L, 5L, "Bông cải xanh", BigDecimal.valueOf(15000),
                        null, "500g", "bông cải xanh", BigDecimal.valueOf(1), "bó",
                        BigDecimal.valueOf(15000), true, false)
        ));

        MealPlan savedPlan = createSavedPlan();

        when(mealPlanRepository.save(any(MealPlan.class))).thenReturn(savedPlan);

        MealPlanResponse response = mealPlanService.generateMealPlan(1L, validRequest);

        assertNotNull(response);
        verify(productMappingService).mapIngredients(anyList());
    }

    // ─── Shopping List Tests ─────────────────────────────────────────────────────

    @Test
    void shouldAggregateDuplicateIngredients() {
        MealPlan plan = new MealPlan();
        plan.setId(1L);
        plan.setUser(testUser);
        plan.setMeals(new ArrayList<>());

        Meal meal = new Meal();
        meal.setId(1L);
        meal.setMealPlan(plan);
        meal.setDayNumber(1);
        meal.setMealType(MealType.BREAKFAST);
        meal.setName("Test Meal");
        meal.setCalories(100);
        meal.setMealNumber(1);
        meal.setProducts(new ArrayList<>());

        Product product = new Product();
        product.setId(5L);
        product.setName("Bông cải xanh");
        product.setPrice(BigDecimal.valueOf(15000));
        product.setUnit("bó");

        // Two meal products for same ingredient
        MealProduct mp1 = new MealProduct();
        mp1.setId(1L);
        mp1.setMeal(meal);
        mp1.setProduct(product);
        mp1.setOriginalIngredientName("bông cải xanh");
        mp1.setQuantity(BigDecimal.valueOf(1));
        mp1.setUnit("bó");
        mp1.setEstimatedPrice(BigDecimal.valueOf(15000));
        mp1.setInStock(true);
        mp1.setAddedToCart(false);

        MealProduct mp2 = new MealProduct();
        mp2.setId(2L);
        mp2.setMeal(meal);
        mp2.setProduct(product);
        mp2.setOriginalIngredientName("bông cải xanh");
        mp2.setQuantity(BigDecimal.valueOf(1));
        mp2.setUnit("bó");
        mp2.setEstimatedPrice(BigDecimal.valueOf(15000));
        mp2.setInStock(true);
        mp2.setAddedToCart(false);

        meal.getProducts().addAll(List.of(mp1, mp2));
        plan.getMeals().add(meal);

        when(mealPlanRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(plan));
        when(mealProductRepository.findByMealPlanId(1L)).thenReturn(List.of(mp1, mp2));

        var result = mealPlanService.getShoppingList(1L, 1L);

        // Should be aggregated to 1 item
        assertEquals(1, result.size());
        assertEquals("bông cải xanh", result.get(0).originalIngredientName());
        assertTrue(result.get(0).isFullyMapped());
        assertTrue(result.get(0).isAnyInStock());
    }

    // ─── Add to Cart Tests ───────────────────────────────────────────────────────

    @Test
    void shouldAddAvailableProductsToCart() {
        MealPlan plan = new MealPlan();
        plan.setId(1L);
        plan.setUser(testUser);

        Product product = new Product();
        product.setId(5L);

        MealProduct mp = new MealProduct();
        mp.setId(1L);
        mp.setProduct(product);
        mp.setOriginalIngredientName("bông cải xanh");
        mp.setQuantity(BigDecimal.valueOf(1));
        mp.setUnit("bó");
        mp.setEstimatedPrice(BigDecimal.valueOf(15000));
        mp.setInStock(true);
        mp.setAddedToCart(false);

        when(mealPlanRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(plan));
        when(mealProductRepository.findAvailableForCart(1L)).thenReturn(List.of(mp));

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUser(testUser);
        cart.setItems(new ArrayList<>());
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        Object result = mealPlanService.addToCart(1L, 1L);

        verify(cartRepository).save(cart);
        assertTrue(cart.getItems().size() > 0);
    }

    @Test
    void shouldThrowWhenNoProductsAvailableForCart() {
        MealPlan plan = new MealPlan();
        plan.setId(1L);
        plan.setUser(testUser);

        when(mealPlanRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(plan));
        when(mealProductRepository.findAvailableForCart(1L)).thenReturn(List.of());

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                mealPlanService.addToCart(1L, 1L));
        assertNotNull(ex.getMessage());
    }

    // ─── AI Error Handling Tests ────────────────────────────────────────────────

    @Test
    void shouldHandleAiTimeout() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userPreferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(geminiAiService.generateMealPlan(any()))
                .thenThrow(new com.bryan.exception.AiTimeoutException("AI timed out"));

        MealPlan savedPlan = new MealPlan();
        savedPlan.setId(1L);
        savedPlan.setUser(testUser);
        savedPlan.setStatus(MealPlanStatus.FAILED);
        when(mealPlanRepository.save(any(MealPlan.class))).thenReturn(savedPlan);

        assertThrows(com.bryan.exception.AiTimeoutException.class, () ->
                mealPlanService.generateMealPlan(1L, validRequest));

        // Verify failed status was saved
        ArgumentCaptor<MealPlan> captor = ArgumentCaptor.forClass(MealPlan.class);
        verify(mealPlanRepository, atLeastOnce()).save(captor.capture());
        assertEquals(MealPlanStatus.FAILED, captor.getValue().getStatus());
    }

    @Test
    void shouldHandleInvalidJsonFromAi() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userPreferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(geminiAiService.generateMealPlan(any()))
                .thenThrow(new com.bryan.exception.AiResponseParseException("Invalid JSON"));

        MealPlan savedPlan = new MealPlan();
        savedPlan.setId(1L);
        savedPlan.setUser(testUser);
        savedPlan.setStatus(MealPlanStatus.FAILED);
        when(mealPlanRepository.save(any(MealPlan.class))).thenReturn(savedPlan);

        assertThrows(com.bryan.exception.AiResponseParseException.class, () ->
                mealPlanService.generateMealPlan(1L, validRequest));
    }

    // ─── Helper Methods ─────────────────────────────────────────────────────────

    private AiDayPlan createAiDay(int dayNumber, List<AiMeal> meals) {
        AiDayPlan day = new AiDayPlan();
        day.dayNumber = dayNumber;
        day.meals = meals;
        return day;
    }

    private AiMeal createAiMeal(String type, String name, List<String> ingredients) {
        AiMeal meal = new AiMeal();
        meal.mealType = type;
        meal.name = name;
        meal.description = "Delicious " + name;
        meal.ingredients = ingredients;
        meal.cookingInstructions = "Step 1: Prepare\nStep 2: Cook";
        meal.preparationMinutes = 10;
        meal.cookingMinutes = 20;
        meal.calories = 300;
        meal.proteinGrams = 25.0;
        meal.carbsGrams = 30.0;
        meal.fatGrams = 10.0;
        return meal;
    }

    private MealPlan createSavedPlan() {
        MealPlan savedPlan = new MealPlan();
        savedPlan.setId(1L);
        savedPlan.setUser(testUser);
        savedPlan.setName("Thực đơn " + LocalDate.now());
        savedPlan.setStartDate(LocalDate.now());
        savedPlan.setNumberOfDays(3);
        savedPlan.setMealsPerDay(3);
        savedPlan.setServings(1);
        savedPlan.setDietType("NORMAL");
        savedPlan.setStatus(MealPlanStatus.COMPLETED);
        savedPlan.setMeals(new ArrayList<>());
        return savedPlan;
    }
}
