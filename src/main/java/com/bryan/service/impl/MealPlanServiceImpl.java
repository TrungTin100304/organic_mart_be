package com.bryan.service.impl;

import com.bryan.dto.request.*;
import com.bryan.dto.response.*;
import com.bryan.entity.*;
import com.bryan.exception.BadRequestException;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.repository.*;
import com.bryan.service.GeminiAiService;
import com.bryan.service.ProductMappingService;
import com.bryan.service.MealPlanRateLimitService;
import com.bryan.service.MealPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MealPlanServiceImpl implements MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final MealRepository mealRepository;
    private final MealProductRepository mealProductRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final AllergenRepository allergenRepository;
    private final GeminiAiService geminiAiService;
    private final ProductMappingService productMappingService;
    private final MealPlanRateLimitService rateLimitService;

    @Override
    public MealPlanResponse generateMealPlan(Long userId, MealPlanGenerationRequest request) {
        rateLimitService.checkRateLimit(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserPreference preference = userPreferenceRepository.findByUserId(userId).orElse(null);

        List<String> userAllergenNames = new ArrayList<>();
        if (user.getAllergens() != null && !user.getAllergens().isEmpty()) {
            userAllergenNames = user.getAllergens().stream()
                    .map(Allergen::getName)
                    .map(String::toLowerCase)
                    .toList();
        }

        // Build AI request
        AiMealRequest aiRequest = new AiMealRequest(
                request.numberOfDays(),
                request.mealsPerDay(),
                request.servings(),
                request.dietType(),
                request.dailyCalorieTarget(),
                request.budgetMax() != null ? request.budgetMax().doubleValue() : null,
                request.maxCookingMinutes(),
                request.preferredIngredients(),
                request.excludedIngredients(),
                request.additionalNotes(),
                userAllergenNames
        );

        // Create placeholder meal plan
        MealPlan mealPlan = new MealPlan();
        mealPlan.setUser(user);
        mealPlan.setName("Thực đơn " + LocalDate.now());
        mealPlan.setStartDate(LocalDate.now());
        mealPlan.setNumberOfDays(request.numberOfDays());
        mealPlan.setMealsPerDay(request.mealsPerDay());
        mealPlan.setServings(request.servings());
        mealPlan.setDietType(request.dietType());
        mealPlan.setDailyCalorieTarget(request.dailyCalorieTarget());
        mealPlan.setBudgetMax(request.budgetMax());
        mealPlan.setMaxCookingMinutes(request.maxCookingMinutes());
        mealPlan.setAdditionalNotes(request.additionalNotes());
        mealPlan.setUserAllergens(userAllergenNames);
        mealPlan.setExcludedIngredients(request.excludedIngredients());
        mealPlan.setStatus(MealPlanStatus.GENERATING);
        mealPlan.setPreferences(Map.of(
                "preferredIngredients", request.preferredIngredients() != null ? request.preferredIngredients() : Collections.emptyList(),
                "excludedIngredients", request.excludedIngredients() != null ? request.excludedIngredients() : Collections.emptyList()
        ));

        mealPlan = mealPlanRepository.save(mealPlan);

        try {
            // Call AI
            AiMealResponse aiResponse = geminiAiService.generateMealPlan(aiRequest);

            // Build meals
            Map<Integer, Integer> dayCalories = new HashMap<>();
            Map<Integer, Integer> dayProtein = new HashMap<>();
            Map<Integer, Integer> dayCarbs = new HashMap<>();
            Map<Integer, Integer> dayFat = new HashMap<>();

            for (AiDayPlan aiDay : aiResponse.days) {
                int dayNum = aiDay.dayNumber;
                int dayCal = 0, dayProt = 0, dayCarb = 0, dayF = 0;

                for (int mi = 0; mi < aiDay.meals.size(); mi++) {
                    AiMeal aiMeal = aiDay.meals.get(mi);

                    Meal meal = new Meal();
                    meal.setDayNumber(dayNum);
                    meal.setMealType(MealType.valueOf(aiMeal.mealType.toUpperCase()));
                    meal.setName(aiMeal.name);
                    meal.setDescription(aiMeal.description);
                    meal.setIngredients(aiMeal.ingredients != null ? aiMeal.ingredients : new ArrayList<>());
                    meal.setCookingInstructions(aiMeal.cookingInstructions);
                    meal.setPreparationMinutes(aiMeal.preparationMinutes);
                    meal.setCookingMinutes(aiMeal.cookingMinutes);
                    meal.setCalories(aiMeal.calories);
                    meal.setProteinGrams(BigDecimal.valueOf(aiMeal.proteinGrams));
                    meal.setCarbsGrams(BigDecimal.valueOf(aiMeal.carbsGrams));
                    meal.setFatGrams(BigDecimal.valueOf(aiMeal.fatGrams));
                    meal.setMealNumber(mi + 1);

                    // Map ingredients to products
                    if (aiMeal.ingredients != null) {
                        List<MealProductResponse> matchedProducts = productMappingService.mapIngredients(aiMeal.ingredients);
                        for (MealProductResponse mp : matchedProducts) {
                            MealProduct mealProduct = new MealProduct();
                            mealProduct.setOriginalIngredientName(mp.originalIngredientName());
                            mealProduct.setProduct(null); // Set product below
                            mealProduct.setQuantity(BigDecimal.ONE);
                            mealProduct.setUnit(mp.unit());
                            mealProduct.setEstimatedPrice(mp.estimatedPrice());
                            mealProduct.setInStock(mp.isInStock());
                            mealProduct.setAddedToCart(false);

                            // Load actual product
                            if (mp.productId() != null) {
                                Product product = new Product();
                                product.setId(mp.productId());
                                mealProduct.setProduct(product);
                            }

                            meal.addProduct(mealProduct);
                        }
                    }

                    mealPlan.addMeal(meal);

                    dayCal += aiMeal.calories;
                    dayProt += Math.round(aiMeal.proteinGrams);
                    dayCarb += Math.round(aiMeal.carbsGrams);
                    dayF += Math.round(aiMeal.fatGrams);
                }

                dayCalories.put(dayNum, dayCal);
                dayProtein.put(dayNum, dayProt);
                dayCarbs.put(dayNum, dayCarb);
                dayFat.put(dayNum, dayF);
            }

            // Set totals (use first day as approximate per-day average)
            int firstDay = dayCalories.keySet().stream().min(Integer::compareTo).orElse(1);
            mealPlan.setTotalCaloriesPerDay(dayCalories.get(firstDay));
            mealPlan.setTotalProteinPerDay(dayProtein.get(firstDay));
            mealPlan.setTotalCarbsPerDay(dayCarbs.get(firstDay));
            mealPlan.setTotalFatPerDay(dayFat.get(firstDay));
            mealPlan.setStatus(MealPlanStatus.COMPLETED);

            mealPlan = mealPlanRepository.save(mealPlan);

            return toResponse(mealPlan);

        } catch (Exception ex) {
            log.error("Failed to generate meal plan for user {}: {}", userId, ex.getMessage(), ex);
            mealPlan.setStatus(MealPlanStatus.FAILED);
            mealPlan.setErrorMessage(ex.getMessage());
            mealPlanRepository.save(mealPlan);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MealPlanResponse> getMealPlans(Long userId) {
        return mealPlanRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MealPlanResponse getMealPlanById(Long id, Long userId) {
        MealPlan mealPlan = mealPlanRepository.findByIdAndUserIdWithDetails(id, userId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy thực đơn hoặc bạn không có quyền truy cập."));
        return toResponse(mealPlan, mealRepository.findByMealPlanIdWithProducts(id));
    }

    @Override
    public void deleteMealPlan(Long id, Long userId) {
        int deletedRows = mealPlanRepository.deleteByIdAndUserId(id, userId);
        if (deletedRows == 0) {
            throw new BadRequestException("Không tìm thấy thực đơn hoặc bạn không có quyền.");
        }
    }

    @Override
    public MealResponse updateMeal(Long mealPlanId, Long mealId, Long userId, MealUpdateRequest request) {
        Meal meal = mealRepository.findByIdAndMealPlanUserId(mealId, userId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy món ăn hoặc bạn không có quyền."));

        if (!meal.getMealPlan().getId().equals(mealPlanId)) {
            throw new BadRequestException("Món ăn không thuộc thực đơn này.");
        }

        meal.setName(request.name());
        meal.setDescription(request.description());
        meal.setIngredients(request.ingredients());
        meal.setCookingInstructions(request.cookingInstructions());
        meal.setPreparationMinutes(request.preparationMinutes());
        meal.setCookingMinutes(request.cookingMinutes());
        meal.setCalories(request.calories());
        meal.setProteinGrams(request.proteinGrams());
        meal.setCarbsGrams(request.carbsGrams());
        meal.setFatGrams(request.fatGrams());

        mealRepository.save(meal);
        return toMealResponse(meal);
    }

    @Override
    public MealResponse regenerateMeal(Long mealPlanId, Long mealId, Long userId) {
        Meal meal = mealRepository.findByIdAndMealPlanUserId(mealId, userId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy món ăn hoặc bạn không có quyền."));

        MealPlan mealPlan = meal.getMealPlan();
        if (!mealPlan.getId().equals(mealPlanId)) {
            throw new BadRequestException("Món ăn không thuộc thực đơn này.");
        }

        // Call AI for a single meal replacement
        AiMealRequest aiRequest = new AiMealRequest(
                1, meal.getMealNumber(), mealPlan.getServings(),
                mealPlan.getDietType(), mealPlan.getDailyCalorieTarget(),
                mealPlan.getBudgetMax() != null ? mealPlan.getBudgetMax().doubleValue() : null,
                mealPlan.getMaxCookingMinutes(),
                null, null, null,
                mealPlan.getUserAllergens() != null
                        ? mealPlan.getUserAllergens().stream().map(String::toLowerCase).toList()
                        : new ArrayList<>()
        );

        try {
            AiMealResponse aiResponse = geminiAiService.generateMealPlan(aiRequest);

            if (aiResponse.days != null && !aiResponse.days.isEmpty()
                    && aiResponse.days.get(0).meals != null
                    && !aiResponse.days.get(0).meals.isEmpty()) {

                AiMeal aiMeal = aiResponse.days.get(0).meals.get(0);
                meal.setName(aiMeal.name);
                meal.setDescription(aiMeal.description);
                meal.setIngredients(aiMeal.ingredients != null ? aiMeal.ingredients : new ArrayList<>());
                meal.setCookingInstructions(aiMeal.cookingInstructions);
                meal.setPreparationMinutes(aiMeal.preparationMinutes);
                meal.setCookingMinutes(aiMeal.cookingMinutes);
                meal.setCalories(aiMeal.calories);
                meal.setProteinGrams(BigDecimal.valueOf(aiMeal.proteinGrams));
                meal.setCarbsGrams(BigDecimal.valueOf(aiMeal.carbsGrams));
                meal.setFatGrams(BigDecimal.valueOf(aiMeal.fatGrams));

                // Update product suggestions
                meal.getProducts().clear();
                if (aiMeal.ingredients != null) {
                    List<MealProductResponse> matchedProducts = productMappingService.mapIngredients(aiMeal.ingredients);
                    for (MealProductResponse mp : matchedProducts) {
                        MealProduct mpEntity = new MealProduct();
                        mpEntity.setOriginalIngredientName(mp.originalIngredientName());
                        mpEntity.setQuantity(BigDecimal.ONE);
                        mpEntity.setUnit(mp.unit());
                        mpEntity.setEstimatedPrice(mp.estimatedPrice());
                        mpEntity.setInStock(mp.isInStock());
                        mpEntity.setAddedToCart(false);
                        if (mp.productId() != null) {
                            Product product = new Product();
                            product.setId(mp.productId());
                            mpEntity.setProduct(product);
                        }
                        meal.addProduct(mpEntity);
                    }
                }

                mealRepository.save(meal);
            }

            return toMealResponse(meal);

        } catch (Exception ex) {
            log.error("Failed to regenerate meal {}: {}", mealId, ex.getMessage());
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShoppingListItemResponse> getShoppingList(Long mealPlanId, Long userId) {
        // Verify ownership
        mealPlanRepository.findByIdAndUserId(mealPlanId, userId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy thực đơn hoặc bạn không có quyền."));

        List<MealProduct> allProducts = mealProductRepository.findByMealPlanId(mealPlanId);

        // Group by original ingredient name
        Map<String, List<MealProduct>> grouped = allProducts.stream()
                .collect(Collectors.groupingBy(mp -> mp.getOriginalIngredientName().toLowerCase().trim()));

        return grouped.values().stream()
                .map(group -> {
                    MealProduct first = group.get(0);
                    String key = first.getOriginalIngredientName().toLowerCase().trim();

                    boolean isFullyMapped = group.stream().allMatch(mp -> mp.getProduct() != null);
                    boolean isAnyInStock = group.stream().anyMatch(MealProduct::isInStock);

                    BigDecimal totalQty = group.stream()
                            .map(MealProduct::getQuantity)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalPrice = group.stream()
                            .map(mp -> mp.getEstimatedPrice() != null ? mp.getEstimatedPrice() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    List<MealProductResponse> products = group.stream()
                            .map(this::toMealProductResponse)
                            .toList();

                    return new ShoppingListItemResponse(
                            key,
                            first.getOriginalIngredientName(),
                            totalQty,
                            first.getUnit(),
                            products,
                            isFullyMapped,
                            isAnyInStock,
                            totalPrice
                    );
                })
                .sorted(Comparator.comparing(ShoppingListItemResponse::originalIngredientName))
                .toList();
    }

    @Override
    public Object addToCart(Long mealPlanId, Long userId) {
        // Verify ownership
        mealPlanRepository.findByIdAndUserId(mealPlanId, userId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy thực đơn hoặc bạn không có quyền."));

        List<MealProduct> availableProducts = mealProductRepository.findAvailableForCart(mealPlanId);

        if (availableProducts.isEmpty()) {
            throw new BadRequestException("Không có sản phẩm nào có sẵn để thêm vào giỏ hàng.");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.getReferenceById(userId);
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        int addedCount = 0;
        for (MealProduct mp : availableProducts) {
            if (mp.getProduct() == null) continue;
            // Check if already in cart
            boolean alreadyInCart = cart.getItems().stream()
                    .anyMatch(item -> item.getProduct().getId().equals(mp.getProduct().getId()));
            if (alreadyInCart) continue;

            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(mp.getProduct());
            cartItem.setQuantity(mp.getQuantity() != null ? mp.getQuantity() : BigDecimal.ONE);
            cart.addItem(cartItem);
            mp.setAddedToCart(true);
            addedCount++;
        }

        if (addedCount > 0) {
            cartRepository.save(cart);
            mealProductRepository.saveAll(availableProducts);
        }

        return Map.of(
                "addedCount", addedCount,
                "message", "Đã thêm " + addedCount + " sản phẩm vào giỏ hàng"
        );
    }

    // ─── Mappers ────────────────────────────────────────────────────────────────

    private MealPlanResponse toResponse(MealPlan mp) {
        return toResponse(mp, mp.getMeals());
    }

    private MealPlanResponse toResponse(MealPlan mp, List<Meal> loadedMeals) {
        Map<Integer, List<Meal>> byDay = loadedMeals.stream()
                .collect(java.util.stream.Collectors.groupingBy(Meal::getDayNumber));

        List<MealDayResponse> days = byDay.entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(entry -> {
                    int dayNum = entry.getKey();
                    List<MealResponse> meals = entry.getValue().stream()
                            .sorted(Comparator.comparingInt(Meal::getMealNumber))
                            .map(meal -> toMealResponse(meal))
                            .toList();

                    int totalCal = meals.stream().mapToInt(MealResponse::getCalories).sum();
                    int totalProt = meals.stream().mapToInt(m -> m.getProteinGrams() != null ? m.getProteinGrams().intValue() : 0).sum();
                    int totalCarbs = meals.stream().mapToInt(m -> m.getCarbsGrams() != null ? m.getCarbsGrams().intValue() : 0).sum();
                    int totalFat = meals.stream().mapToInt(m -> m.getFatGrams() != null ? m.getFatGrams().intValue() : 0).sum();

                    return new MealDayResponse(dayNum, meals, totalCal, totalProt, totalCarbs, totalFat);
                })
                .toList();

        return new MealPlanResponse(
                mp.getId(),
                mp.getName(),
                mp.getStartDate() != null ? mp.getStartDate().toString() : null,
                mp.getNumberOfDays(),
                mp.getMealsPerDay(),
                mp.getServings(),
                mp.getDietType(),
                mp.getDailyCalorieTarget(),
                mp.getBudgetMax(),
                mp.getMaxCookingMinutes(),
                mp.getAdditionalNotes(),
                mp.getStatus(),
                days,
                mp.getTotalCaloriesPerDay(),
                mp.getTotalProteinPerDay(),
                mp.getTotalCarbsPerDay(),
                mp.getTotalFatPerDay(),
                mp.getErrorMessage(),
                mp.getCreatedAt()
        );
    }

    private MealResponse toMealResponse(Meal meal) {
        List<MealProductResponse> products = meal.getProducts().stream()
                .map(this::toMealProductResponse)
                .toList();

        return new MealResponse(
                meal.getId(),
                meal.getMealType(),
                meal.getName(),
                meal.getDescription(),
                meal.getIngredients(),
                meal.getCookingInstructions(),
                meal.getPreparationMinutes(),
                meal.getCookingMinutes(),
                meal.getCalories() != null ? meal.getCalories() : 0,
                meal.getProteinGrams(),
                meal.getCarbsGrams(),
                meal.getFatGrams(),
                products
        );
    }

    private MealProductResponse toMealProductResponse(MealProduct mp) {
        return new MealProductResponse(
                mp.getId(),
                mp.getProduct() != null ? mp.getProduct().getId() : null,
                mp.getProduct() != null ? mp.getProduct().getName() : null,
                mp.getProduct() != null ? mp.getProduct().getPrice() : null,
                mp.getProduct() != null ? mp.getProduct().getImageUrl() : null,
                mp.getProduct() != null ? mp.getProduct().getUnit() : null,
                mp.getOriginalIngredientName(),
                mp.getQuantity(),
                mp.getUnit(),
                mp.getEstimatedPrice(),
                mp.isInStock(),
                mp.isAddedToCart()
        );
    }
}
