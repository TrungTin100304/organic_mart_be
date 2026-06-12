package com.bryan.service;

import com.bryan.dto.response.AdminDashboardResponse;

public interface AdminDashboardService {
    AdminDashboardResponse getDashboard(int days);
}
