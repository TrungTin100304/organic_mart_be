package com.bryan.service;

import com.bryan.dto.request.AdminPromotionRequest;
import com.bryan.dto.response.AdminPromotionResponse;

import java.util.List;

public interface AdminPromotionService {
    List<AdminPromotionResponse> getAll();
    AdminPromotionResponse create(AdminPromotionRequest request);
    AdminPromotionResponse update(Long id, AdminPromotionRequest request);
    AdminPromotionResponse deactivate(Long id);
}
