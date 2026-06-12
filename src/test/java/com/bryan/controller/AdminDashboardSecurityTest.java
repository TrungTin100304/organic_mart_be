package com.bryan.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminDashboardSecurityTest {

    @Test
    void dashboardControllerShouldBeAdminOnly() throws Exception {
        Class<?> controller = Class.forName("com.bryan.controller.admin.AdminDashboardController");
        PreAuthorize annotation = controller.getAnnotation(PreAuthorize.class);
        assertEquals("hasRole('ADMIN')", annotation.value());
    }
}
