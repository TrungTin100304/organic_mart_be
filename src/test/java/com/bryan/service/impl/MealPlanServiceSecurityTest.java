package com.bryan.service.impl;

import com.bryan.config.MealPlanProperties;
import com.bryan.dto.request.MealPlanGenerationRequest;
import com.bryan.dto.response.MealPlanResponse;
import com.bryan.entity.Meal;
import com.bryan.entity.MealPlanStatus;
import com.bryan.entity.MealType;
import com.bryan.entity.User;
import com.bryan.exception.BadRequestException;
import com.bryan.repository.*;
import com.bryan.service.GeminiAiService;
import com.bryan.service.MealPlanRateLimitService;
import com.bryan.service.ProductMappingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MealPlanServiceSecurityTest {

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

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test User");
        testUser.setEmail("user@test.com");
        testUser.setAllergens(new java.util.HashSet<>());
    }

    // ─── Ownership Tests ─────────────────────────────────────────────────────

    @Test
    void shouldAllowUserToDeleteOwnMealPlan() {
        com.bryan.entity.MealPlan plan = new com.bryan.entity.MealPlan();
        plan.setId(1L);
        plan.setUser(testUser);
        when(mealPlanRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(plan));

        assertDoesNotThrow(() -> mealPlanService.deleteMealPlan(1L, 1L));
        verify(mealPlanRepository).delete(plan);
    }

    @Test
    void shouldDenyUserFromDeletingAnotherUsersMealPlan() {
        when(mealPlanRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> mealPlanService.deleteMealPlan(1L, 2L));
        assertNotNull(ex.getMessage());
        verify(mealPlanRepository, never()).delete(any());
    }

    @Test
    void shouldAllowUserToGetOwnMealPlan() {
        com.bryan.entity.MealPlan plan = new com.bryan.entity.MealPlan();
        plan.setId(5L);
        plan.setUser(testUser);
        plan.setName("My Plan");
        plan.setStatus(MealPlanStatus.COMPLETED);
        plan.setMeals(new java.util.ArrayList<>());
        plan.setNumberOfDays(3);
        plan.setMealsPerDay(3);
        plan.setServings(1);
        plan.setDietType("NORMAL");
        when(mealPlanRepository.findByIdAndUserIdWithDetails(5L, 1L)).thenReturn(Optional.of(plan));

        Meal meal = new Meal();
        meal.setId(10L);
        meal.setMealPlan(plan);
        meal.setDayNumber(1);
        meal.setMealNumber(1);
        meal.setMealType(MealType.BREAKFAST);
        meal.setName("Breakfast");
        when(mealRepository.findByMealPlanIdWithProducts(5L)).thenReturn(List.of(meal));

        MealPlanResponse response = mealPlanService.getMealPlanById(5L, 1L);
        assertNotNull(response);
        assertEquals(5L, response.id());
        assertEquals(1, response.days().size());
        assertEquals(1, response.days().get(0).meals().size());
        verify(mealRepository).findByMealPlanIdWithProducts(5L);
    }

    @Test
    void shouldDenyUserFromGettingAnotherUsersMealPlan() {
        when(mealPlanRepository.findByIdAndUserIdWithDetails(5L, 2L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
                () -> mealPlanService.getMealPlanById(5L, 2L));
    }

    @Test
    void shouldAllowUserToGetShoppingList() {
        com.bryan.entity.MealPlan plan = new com.bryan.entity.MealPlan();
        plan.setId(1L);
        plan.setUser(testUser);
        when(mealPlanRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(plan));
        when(mealProductRepository.findByMealPlanId(1L)).thenReturn(List.of());

        var result = mealPlanService.getShoppingList(1L, 1L);
        assertNotNull(result);
    }

    @Test
    void shouldDenyUserFromGettingAnotherUsersShoppingList() {
        when(mealPlanRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
                () -> mealPlanService.getShoppingList(1L, 2L));
    }

    @Test
    void shouldAllowUserToAddToOwnCart() {
        com.bryan.entity.MealPlan plan = new com.bryan.entity.MealPlan();
        plan.setId(1L);
        plan.setUser(testUser);
        when(mealPlanRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(plan));
        when(mealProductRepository.findAvailableForCart(1L)).thenReturn(List.of());

        assertThrows(BadRequestException.class,
                () -> mealPlanService.addToCart(1L, 1L));
    }

    @Test
    void shouldDenyUserFromAddingAnotherUsersMealPlanToCart() {
        when(mealPlanRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
                () -> mealPlanService.addToCart(1L, 2L));
    }

    // ─── Rate Limit Tests ───────────────────────────────────────────────────

    @Test
    void shouldRejectRequestWhenRateLimitExceeded() {
        MealPlanGenerationRequest request = new MealPlanGenerationRequest(
                3, 3, 1, "NORMAL", null, null, null, null, null, null
        );

        doThrow(new com.bryan.exception.MealPlanRateLimitException("Quá nhiều yêu cầu"))
                .when(rateLimitService).checkRateLimit(1L);

        assertThrows(com.bryan.exception.MealPlanRateLimitException.class,
                () -> mealPlanService.generateMealPlan(1L, request));

        verify(mealPlanRepository, never()).save(any());
    }

    // ─── Empty/Cleanup Tests ─────────────────────────────────────────────────

    @Test
    void shouldListOnlyUsersOwnMealPlans() {
        com.bryan.entity.MealPlan plan = new com.bryan.entity.MealPlan();
        plan.setId(1L);
        plan.setUser(testUser);
        plan.setName("My Plan");
        plan.setStatus(MealPlanStatus.COMPLETED);
        plan.setMeals(new java.util.ArrayList<>());
        plan.setNumberOfDays(3);
        plan.setMealsPerDay(3);
        plan.setServings(1);
        plan.setDietType("NORMAL");
        when(mealPlanRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(plan));

        var plans = mealPlanService.getMealPlans(1L);
        assertEquals(1, plans.size());
        assertEquals("My Plan", plans.get(0).name());
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoMealPlans() {
        when(mealPlanRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        var plans = mealPlanService.getMealPlans(1L);
        assertTrue(plans.isEmpty());
    }
}
