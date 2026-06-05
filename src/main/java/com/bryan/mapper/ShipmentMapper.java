package com.bryan.mapper;

import com.bryan.dto.response.ShipmentListResponse;
import com.bryan.dto.response.ShipmentResponse;
import com.bryan.dto.response.ShipmentTrackingResponse;
import com.bryan.entity.Shipment;
import com.bryan.entity.ShipmentTracking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {ShippingProviderMapper.class})
public interface ShipmentMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "order.orderCode", target = "orderCode")
    ShipmentResponse toResponse(Shipment shipment);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "location", target = "location")
    @Mapping(source = "loggedAt", target = "loggedAt")
    ShipmentTrackingResponse toTrackingResponse(ShipmentTracking tracking);

    default ShipmentListResponse toListResponse(Shipment shipment) {
        return new ShipmentListResponse(
            shipment.getId(),
            shipment.getOrder() != null ? shipment.getOrder().getId() : null,
            shipment.getOrder() != null ? shipment.getOrder().getOrderCode() : null,
            shipment.getProvider() != null ? shipment.getProvider().getName() : null,
            shipment.getTrackingCode(),
            shipment.getShippingFee(),
            shipment.getCurrentStatus(),
            shipment.getCreatedAt()
        );
    }
}
