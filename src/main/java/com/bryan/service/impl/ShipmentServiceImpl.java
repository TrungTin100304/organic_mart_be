package com.bryan.service.impl;

import com.bryan.dto.request.AddTrackingRequest;
import com.bryan.dto.request.CalculateShippingRateRequest;
import com.bryan.dto.request.CreateShipmentRequest;
import com.bryan.dto.request.UpdateShipmentRequest;
import com.bryan.dto.response.ShipmentListResponse;
import com.bryan.dto.response.ShipmentResponse;
import com.bryan.dto.response.ShipmentTrackingResponse;
import com.bryan.dto.response.ShippingRateResponse;
import com.bryan.entity.*;
import com.bryan.exception.BadRequestException;
import com.bryan.exception.ResourceNotFoundException;
import com.bryan.mapper.ShipmentMapper;
import com.bryan.repository.*;
import com.bryan.security.CustomUserDetails;
import com.bryan.service.ShipmentService;
import com.bryan.service.ShippingProviderApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final ShippingProviderRepository providerRepository;
    private final ShipmentTrackingRepository trackingRepository;
    private final UserRepository userRepository;
    private final ShipmentMapper shipmentMapper;
    private final List<ShippingProviderApiService> shippingApiServiceList;

    @Override
    public List<ShippingRateResponse> calculateAllRates(String province, String district, String ward, BigDecimal weightKg) {
        return providerRepository.findByIsActiveTrue().stream()
            .map(provider -> {
                ShippingProviderApiService apiService = getApiService(provider.getName());
                ShippingProviderApiService.ShippingRateRequest apiRequest = new ShippingProviderApiService.ShippingRateRequest(
                    province, district, ward, weightKg);
                ShippingProviderApiService.ShippingRateResponse apiResponse = apiService.calculateRate(apiRequest);
                return new ShippingRateResponse(provider.getId(), apiResponse.providerName(), apiResponse.fee(), apiResponse.estimatedDays());
            })
            .toList();
    }

    @Override
    public ShippingRateResponse calculateRate(CalculateShippingRateRequest request) {
        ShippingProvider provider = providerRepository.findById(request.providerId())
            .orElseThrow(() -> new ResourceNotFoundException("Shipping provider not found with id: " + request.providerId()));

        if (!provider.getIsActive()) {
            throw new BadRequestException("Shipping provider is not active");
        }

        ShippingProviderApiService apiService = getApiService(provider.getName());
        ShippingProviderApiService.ShippingRateRequest apiRequest = new ShippingProviderApiService.ShippingRateRequest(
            request.province(),
            request.district(),
            request.ward(),
            request.weightKg()
        );

        ShippingProviderApiService.ShippingRateResponse apiResponse = apiService.calculateRate(apiRequest);

        return new ShippingRateResponse(
            provider.getId(),
            apiResponse.providerName(),
            apiResponse.fee(),
            apiResponse.estimatedDays()
        );
    }

    @Override
    public ShipmentResponse createShipment(CreateShipmentRequest request) {
        if (shipmentRepository.existsByOrderId(request.orderId())) {
            throw new BadRequestException("Shipment already exists for order id: " + request.orderId());
        }

        Order order = orderRepository.findById(request.orderId())
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.orderId()));

        if (order.getStatus() != OrderStatus.PREPARING) {
            throw new BadRequestException("Cannot create shipment for order with status: " + order.getStatus() + ". Order must be in PREPARING status.");
        }

        ShippingProvider provider = providerRepository.findById(request.providerId())
            .orElseThrow(() -> new ResourceNotFoundException("Shipping provider not found with id: " + request.providerId()));

        if (!provider.getIsActive()) {
            throw new BadRequestException("Shipping provider is not active");
        }

        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setProvider(provider);
        shipment.setTrackingCode(request.trackingCode());
        shipment.setShippingFee(request.shippingFee());
        shipment.setCurrentStatus(ShipmentStatus.PREPARING);

        ShipmentTracking initialTracking = new ShipmentTracking();
        initialTracking.setStatus(ShipmentStatus.PREPARING);
        initialTracking.setNote("Shipment created via " + provider.getName());
        initialTracking.setLocation("System");
        shipment.addTracking(initialTracking);

        syncOrderFromShipment(shipment, "Shipment created via " + provider.getName());

        Shipment saved = shipmentRepository.save(shipment);
        log.info("Shipment created for order {} with status PREPARING", order.getOrderCode());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentListResponse> getAllShipments(Pageable pageable) {
        return shipmentRepository.findAllShipments(pageable)
            .map(shipmentMapper::toListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentListResponse> getShipmentsByStatus(ShipmentStatus status, Pageable pageable) {
        return shipmentRepository.findByStatus(status, pageable)
            .map(shipmentMapper::toListResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentById(Long id) {
        Shipment shipment = findById(id);
        return mapToResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByOrderId(Long orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for order id: " + orderId));
        return mapToResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByTrackingCode(String trackingCode) {
        Shipment shipment = shipmentRepository.findByTrackingCode(trackingCode)
            .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with tracking code: " + trackingCode));
        return mapToResponse(shipment);
    }

    @Override
    public ShipmentResponse updateShipment(Long id, UpdateShipmentRequest request) {
        Shipment shipment = findById(id);

        ShippingProvider provider = providerRepository.findById(request.providerId())
            .orElseThrow(() -> new ResourceNotFoundException("Shipping provider not found with id: " + request.providerId()));

        if (!provider.getIsActive()) {
            throw new BadRequestException("Shipping provider is not active");
        }

        validateStatusTransition(shipment.getCurrentStatus(), request.currentStatus());

        shipment.setProvider(provider);
        shipment.setTrackingCode(request.trackingCode());

        boolean feeChanged = shipment.getShippingFee().compareTo(request.shippingFee()) != 0;
        BigDecimal oldFee = shipment.getShippingFee();
        shipment.setShippingFee(request.shippingFee());

        ShipmentStatus previousStatus = shipment.getCurrentStatus();
        shipment.setCurrentStatus(request.currentStatus());

        if (request.currentStatus() == ShipmentStatus.PICKED_UP ||
            request.currentStatus() == ShipmentStatus.IN_TRANSIT ||
            request.currentStatus() == ShipmentStatus.OUT_FOR_DELIVERY) {
            shipment.setShippedAt(LocalDateTime.now());
        }
        if (request.currentStatus() == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(LocalDateTime.now());
        }

        ShipmentTracking statusTracking = new ShipmentTracking();
        statusTracking.setStatus(request.currentStatus());
        statusTracking.setNote("Status updated from " + previousStatus + " to " + request.currentStatus());
        shipment.addTracking(statusTracking);

        String note = feeChanged
            ? "Shipment updated: " + previousStatus + " → " + request.currentStatus() + " | Fee: " + oldFee + " → " + request.shippingFee()
            : "Shipment updated: " + previousStatus + " → " + request.currentStatus();
        syncOrderFromShipment(shipment, note);

        Shipment saved = shipmentRepository.save(shipment);
        log.info("Shipment {} updated to status {}", id, request.currentStatus());
        return mapToResponse(saved);
    }

    @Override
    public void deleteShipment(Long id) {
        if (!shipmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shipment not found with id: " + id);
        }
        shipmentRepository.deleteById(id);
        log.info("Shipment {} deleted", id);
    }

    @Override
    public ShipmentResponse addTracking(Long shipmentId, AddTrackingRequest request) {
        Shipment shipment = findById(shipmentId);

        validateStatusTransition(shipment.getCurrentStatus(), request.status());

        ShipmentStatus previousStatus = shipment.getCurrentStatus();
        shipment.setCurrentStatus(request.status());

        if (request.status() == ShipmentStatus.PICKED_UP ||
            request.status() == ShipmentStatus.IN_TRANSIT ||
            request.status() == ShipmentStatus.OUT_FOR_DELIVERY) {
            shipment.setShippedAt(LocalDateTime.now());
        }
        if (request.status() == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(LocalDateTime.now());
        }

        ShipmentTracking tracking = new ShipmentTracking();
        tracking.setStatus(request.status());
        tracking.setLocation(request.location());
        tracking.setNote(request.note());
        shipment.addTracking(tracking);

        String historyNote = "Shipment tracking: " + previousStatus + " → " + request.status()
            + (request.location() != null ? " at " + request.location() : "")
            + (request.note() != null ? " | " + request.note() : "");
        syncOrderFromShipment(shipment, historyNote);

        Shipment saved = shipmentRepository.save(shipment);
        log.info("Tracking added to shipment {}: {} -> {} at {}",
            shipmentId, previousStatus, request.status(), request.location());
        return mapToResponse(saved);
    }

    private Shipment findById(Long id) {
        return shipmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));
    }

    private void validateStatusTransition(ShipmentStatus from, ShipmentStatus to) {
        boolean valid = switch (to) {
            case PREPARING -> false;
            case PICKED_UP -> from == ShipmentStatus.PREPARING;
            case IN_TRANSIT -> from == ShipmentStatus.PICKED_UP || from == ShipmentStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> from == ShipmentStatus.IN_TRANSIT;
            case DELIVERED -> from == ShipmentStatus.OUT_FOR_DELIVERY || from == ShipmentStatus.IN_TRANSIT;
            case RETURNED -> from == ShipmentStatus.IN_TRANSIT || from == ShipmentStatus.OUT_FOR_DELIVERY;
            case FAILED -> from == ShipmentStatus.OUT_FOR_DELIVERY;
        };
        if (!valid) {
            throw new BadRequestException(
                "Cannot change shipment status from " + from + " to " + to);
        }
    }

    private ShipmentResponse mapToResponse(Shipment shipment) {
        ShipmentResponse basic = shipmentMapper.toResponse(shipment);

        var trackings = shipment.getTrackings().stream()
            .map(t -> new ShipmentTrackingResponse(
                t.getId(),
                t.getStatus(),
                t.getNote(),
                t.getLocation(),
                t.getLoggedAt()))
            .toList();

        return new ShipmentResponse(
            basic.id(),
            basic.orderId(),
            basic.orderCode(),
            basic.provider(),
            basic.trackingCode(),
            basic.shippingFee(),
            basic.currentStatus(),
            basic.shippedAt(),
            basic.deliveredAt(),
            trackings,
            basic.createdAt()
        );
    }

    private void syncOrderFromShipment(Shipment shipment, String historyNote) {
        Order order = shipment.getOrder();
        OrderStatus fromStatus = order.getStatus();

        boolean shouldUpdate = false;
        ShipmentStatus shipmentStatus = shipment.getCurrentStatus();

        if (shipmentStatus == ShipmentStatus.PICKED_UP ||
            shipmentStatus == ShipmentStatus.IN_TRANSIT ||
            shipmentStatus == ShipmentStatus.OUT_FOR_DELIVERY) {
            if (fromStatus == OrderStatus.READY_FOR_DELIVERY) {
                order.setStatus(OrderStatus.DELIVERING);
                shouldUpdate = true;
            }
        } else if (shipmentStatus == ShipmentStatus.DELIVERED) {
            if (fromStatus == OrderStatus.DELIVERING) {
                order.setStatus(OrderStatus.DELIVERED);
                shouldUpdate = true;
            }
        }

        if (shouldUpdate) {
            OrderStatus toStatus = order.getStatus();
            User admin = getCurrentUser().orElse(null);

            OrderStatusHistory history = new OrderStatusHistory();
            history.setFromStatus(fromStatus);
            history.setToStatus(toStatus);
            history.setChangedBy(admin);
            history.setNote(historyNote);
            order.addStatusHistory(history);

            log.info("Order {} status auto-synced from Shipment: {} -> {}",
                order.getOrderCode(), fromStatus, toStatus);
        }
    }

    private Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails ud) {
            return userRepository.findById(ud.getId());
        }
        return Optional.empty();
    }

    private ShippingProviderApiService getApiService(String providerName) {
        String normalizedProviderName = providerName == null
            ? ""
            : providerName.trim().toUpperCase(Locale.ROOT);

        Map<String, ShippingProviderApiService> providersByName = shippingApiServiceList.stream()
            .collect(Collectors.toMap(
                service -> service.getProviderName().trim().toUpperCase(Locale.ROOT),
                Function.identity(),
                (first, second) -> first
            ));

        ShippingProviderApiService service = providersByName.get(normalizedProviderName);
        if (service == null) {
            throw new BadRequestException("No API service registered for provider: " + providerName);
        }
        return service;
    }
}
