package com.bryan.service;

import com.bryan.dto.request.AddTrackingRequest;
import com.bryan.dto.request.CalculateShippingRateRequest;
import com.bryan.dto.request.CreateShipmentRequest;
import com.bryan.dto.request.UpdateShipmentRequest;
import com.bryan.dto.response.ShipmentListResponse;
import com.bryan.dto.response.ShipmentResponse;
import com.bryan.dto.response.ShippingRateResponse;
import com.bryan.entity.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ShipmentService {

    List<ShippingRateResponse> calculateAllRates(String province, String district, String ward, BigDecimal weightKg);

    ShippingRateResponse calculateRate(CalculateShippingRateRequest request);

    ShipmentResponse createShipment(CreateShipmentRequest request);

    Page<ShipmentListResponse> getAllShipments(Pageable pageable);

    Page<ShipmentListResponse> getShipmentsByStatus(ShipmentStatus status, Pageable pageable);

    ShipmentResponse getShipmentById(Long id);

    ShipmentResponse getShipmentByOrderId(Long orderId);

    ShipmentResponse getShipmentByTrackingCode(String trackingCode);

    ShipmentResponse updateShipment(Long id, UpdateShipmentRequest request);

    void deleteShipment(Long id);

    ShipmentResponse addTracking(Long shipmentId, AddTrackingRequest request);
}
