package com.bryan.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminPromotionSecurityTest {

    @Test
    void promotionControllerShouldBeAdminOnly() throws Exception {
        Class<?> controller = Class.forName("com.bryan.controller.admin.AdminPromotionController");
        PreAuthorize annotation = controller.getAnnotation(PreAuthorize.class);
        assertEquals("hasRole('ADMIN')", annotation.value());
    }
}
