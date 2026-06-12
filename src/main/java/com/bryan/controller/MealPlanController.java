package com.bryan.controller;

import com.bryan.dto.request.MealPlanGenerationRequest;
import com.bryan.dto.request.MealUpdateRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.MealPlanResponse;
import com.bryan.dto.response.MealResponse;
import com.bryan.dto.response.ShoppingListItemResponse;
import com.bryan.entity.User;
import com.bryan.repository.UserRepository;
import com.bryan.service.MealPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/meal-plans")
@RequiredArgsConstructor
public class MealPlanController {

    private final MealPlanService mealPlanService;
    private final UserRepository userRepository;

    @PostMapping("/generate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MealPlanResponse>> generateMealPlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MealPlanGenerationRequest request) {

        Long userId = getCurrentUserId(userDetails);
        MealPlanResponse result = mealPlanService.generateMealPlan(userId, request);
        return ApiResponse.success(result, "Tạo thực đơn thành công");
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MealPlanResponse>>> getMealPlans(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = getCurrentUserId(userDetails);
        List<MealPlanResponse> plans = mealPlanService.getMealPlans(userId);
        return ApiResponse.success(plans);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MealPlanResponse>> getMealPlanById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long userId = getCurrentUserId(userDetails);
        MealPlanResponse plan = mealPlanService.getMealPlanById(id, userId);
        return ApiResponse.success(plan);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteMealPlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long userId = getCurrentUserId(userDetails);
        mealPlanService.deleteMealPlan(id, userId);
        return ApiResponse.success(200, null, "Xóa thực đơn thành công");
    }

    @PutMapping("/{mealPlanId}/meals/{mealId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MealResponse>> updateMeal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long mealPlanId,
            @PathVariable Long mealId,
            @Valid @RequestBody MealUpdateRequest request) {

        Long userId = getCurrentUserId(userDetails);
        MealResponse meal = mealPlanService.updateMeal(mealPlanId, mealId, userId, request);
        return ApiResponse.success(meal);
    }

    @PostMapping("/{mealPlanId}/meals/{mealId}/regenerate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MealResponse>> regenerateMeal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long mealPlanId,
            @PathVariable Long mealId) {

        Long userId = getCurrentUserId(userDetails);
        MealResponse meal = mealPlanService.regenerateMeal(mealPlanId, mealId, userId);
        return ApiResponse.success(meal, "Tạo lại món ăn thành công");
    }

    @GetMapping("/{id}/shopping-list")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ShoppingListItemResponse>>> getShoppingList(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long userId = getCurrentUserId(userDetails);
        List<ShoppingListItemResponse> list = mealPlanService.getShoppingList(id, userId);
        return ApiResponse.success(list);
    }

    @PostMapping("/{id}/add-to-cart")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Object>> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long userId = getCurrentUserId(userDetails);
        Object result = mealPlanService.addToCart(id, userId);
        return ApiResponse.success(result, "Đã thêm vào giỏ hàng");
    }

    private Long getCurrentUserId(UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
