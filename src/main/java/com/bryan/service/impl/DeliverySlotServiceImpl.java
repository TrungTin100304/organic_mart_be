package com.bryan.service.impl;

import com.bryan.dto.request.DeliverySlotRequest;
import com.bryan.dto.request.StatusRequest;
import com.bryan.dto.response.AvailableSlotResponse;
import com.bryan.dto.response.DeliverySlotResponse;
import com.bryan.entity.DeliverySlot;
import com.bryan.exception.BadRequestException;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.repository.DeliverySlotRepository;
import com.bryan.repository.OrderRepository;
import com.bryan.service.DeliverySlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeliverySlotServiceImpl implements DeliverySlotService {

    private final DeliverySlotRepository slotRepository;
    private final OrderRepository orderRepository;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    @Transactional(readOnly = true)
    public List<DeliverySlotResponse> getAllSlots() {
        return slotRepository.findAll().stream()
            .map(DeliverySlotResponse::from)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliverySlotResponse> getActiveSlots() {
        return slotRepository.findActiveSlotsOrdered().stream()
            .map(DeliverySlotResponse::from)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DeliverySlotResponse getSlotById(Long id) {
        return slotRepository.findById(id)
            .map(DeliverySlotResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery slot not found: " + id));
    }

    @Override
    public DeliverySlotResponse createSlot(DeliverySlotRequest request) {
        DeliverySlot slot = mapRequestToEntity(request, new DeliverySlot());
        DeliverySlot saved = slotRepository.save(slot);
        log.info("Created delivery slot: id={}, name={}", saved.getId(), saved.getName());
        return DeliverySlotResponse.from(saved);
    }

    @Override
    public DeliverySlotResponse updateSlot(Long id, DeliverySlotRequest request) {
        DeliverySlot slot = slotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery slot not found: " + id));
        mapRequestToEntity(request, slot);
        DeliverySlot saved = slotRepository.save(slot);
        log.info("Updated delivery slot: id={}, name={}", saved.getId(), saved.getName());
        return DeliverySlotResponse.from(saved);
    }

    @Override
    public DeliverySlotResponse updateStatus(Long id, StatusRequest request) {
        DeliverySlot slot = slotRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Delivery slot not found: " + id));
        slot.setIsActive(request.isActive());
        DeliverySlot saved = slotRepository.save(slot);
        log.info("Updated delivery slot status: id={}, isActive={}", saved.getId(), saved.getIsActive());
        return DeliverySlotResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailableSlotResponse> getAvailableSlots(LocalDate date) {
        List<DeliverySlot> activeSlots = slotRepository.findActiveSlotsOrdered();
        LocalDateTime now = LocalDateTime.now();
        boolean isToday = date.equals(now.toLocalDate());

        return activeSlots.stream().map(slot -> {
            int remaining = slot.getMaximumOrders();
            String unavailableReason = null;

            long currentCount = slotRepository.countOrdersForSlotOnDate(slot.getId(), date);
            remaining = slot.getMaximumOrders() - (int) currentCount;

            if (remaining <= 0) {
                unavailableReason = "Khung giờ đã đầy";
            } else if (isToday) {
                LocalDateTime slotStart = LocalDateTime.of(date, slot.getStartTime());
                LocalDateTime cutoff = slotStart.minusMinutes(slot.getCutoffMinutes());
                if (now.isAfter(cutoff)) {
                    unavailableReason = "Đã hết thời gian đặt cho khung giờ này";
                }
            }

            boolean available = unavailableReason == null && remaining > 0;
            return new AvailableSlotResponse(
                slot.getId(),
                slot.getName(),
                slot.getStartTime(),
                slot.getEndTime(),
                remaining,
                available,
                unavailableReason
            );
        }).toList();
    }

    private DeliverySlot mapRequestToEntity(DeliverySlotRequest request, DeliverySlot slot) {
        slot.setName(request.name());
        slot.setStartTime(parseTime(request.startTime(), "startTime"));
        slot.setEndTime(parseTime(request.endTime(), "endTime"));
        if (request.cutoffMinutes() != null) {
            slot.setCutoffMinutes(request.cutoffMinutes());
        }
        if (request.maximumOrders() != null) {
            slot.setMaximumOrders(request.maximumOrders());
        }
        if (request.displayOrder() != null) {
            slot.setDisplayOrder(request.displayOrder());
        }
        return slot;
    }

    private LocalTime parseTime(String time, String fieldName) {
        try {
            return LocalTime.parse(time, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new BadRequestException(fieldName + " must be in HH:mm format (e.g., 08:00)");
        }
    }
}
