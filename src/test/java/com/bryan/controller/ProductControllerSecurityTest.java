package com.bryan.controller;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductControllerSecurityTest {

    @Test
    void productMutationsShouldRequireAdminRole() throws NoSuchMethodException {
        assertAdminOnly(ProductController.class.getMethod(
                "createProduct", com.bryan.dto.request.ProductRequest.class));
        assertAdminOnly(ProductController.class.getMethod(
                "updateProduct", Long.class, com.bryan.dto.request.ProductRequest.class));
        assertAdminOnly(ProductController.class.getMethod("deleteProduct", Long.class));
    }

    private void assertAdminOnly(Method method) {
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertEquals("hasRole('ADMIN')", annotation.value());
    }
}
