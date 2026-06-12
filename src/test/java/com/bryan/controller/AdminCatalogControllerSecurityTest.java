package com.bryan.controller;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AdminCatalogControllerSecurityTest {

    @Test
    void catalogMutationsShouldRequireAdminRole() throws NoSuchMethodException {
        assertAdminOnly(AllergenController.class.getMethod(
                "createAllergen", com.bryan.dto.request.AllergenRequest.class));
        assertAdminOnly(AllergenController.class.getMethod(
                "updateAllergen", Long.class, com.bryan.dto.request.AllergenRequest.class));
        assertAdminOnly(AllergenController.class.getMethod("deleteAllergen", Long.class));

        assertAdminOnly(FarmController.class.getMethod(
                "createFarm", com.bryan.dto.request.FarmRequest.class));
        assertAdminOnly(FarmController.class.getMethod(
                "updateFarm", Long.class, com.bryan.dto.request.FarmRequest.class));
        assertAdminOnly(FarmController.class.getMethod("deleteFarm", Long.class));

        assertAdminOnly(InventoryBatchController.class.getMethod(
                "createBatch", com.bryan.dto.request.InventoryBatchRequest.class));
        assertAdminOnly(InventoryBatchController.class.getMethod(
                "updateBatch", Long.class, com.bryan.dto.request.InventoryBatchRequest.class));
        assertAdminOnly(InventoryBatchController.class.getMethod("deleteBatch", Long.class));

        assertAdminOnly(ProductCategoryController.class.getMethod(
                "createCategory", com.bryan.dto.request.ProductCategoryRequest.class));
        assertAdminOnly(ProductCategoryController.class.getMethod(
                "updateCategory", Long.class, com.bryan.dto.request.ProductCategoryRequest.class));
        assertAdminOnly(ProductCategoryController.class.getMethod("deleteCategory", Long.class));
    }

    private void assertAdminOnly(Method method) {
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, method + " must declare @PreAuthorize");
        assertEquals("hasRole('ADMIN')", annotation.value());
    }
}
