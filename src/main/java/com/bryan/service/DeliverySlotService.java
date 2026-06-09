package com.bryan.service;

import com.bryan.dto.request.DeliverySlotRequest;
import com.bryan.dto.request.StatusRequest;
import com.bryan.dto.response.AvailableSlotResponse;
import com.bryan.dto.response.DeliverySlotResponse;

import java.time.LocalDate;
import java.util.List;

public interface DeliverySlotService {
    List<DeliverySlotResponse> getAllSlots();
    List<DeliverySlotResponse> getActiveSlots();
    DeliverySlotResponse getSlotById(Long id);
    DeliverySlotResponse createSlot(DeliverySlotRequest request);
    DeliverySlotResponse updateSlot(Long id, DeliverySlotRequest request);
    DeliverySlotResponse updateStatus(Long id, StatusRequest request);
    List<AvailableSlotResponse> getAvailableSlots(LocalDate date);
}
