package com.bryan.dto.response;

import com.bryan.entity.AddressLabel;
import com.bryan.entity.UserAddress;

import java.time.LocalDateTime;

public record UserAddressResponse(
    Long id,
    AddressLabel label,
    String customLabel,
    String recipientName,
    String recipientPhone,
    String fullAddress,
    String ward,
    String district,
    String city,
    boolean isDefault,
    Long buildingId,
    String buildingCode,
    String buildingName,
    String floor,
    String apartmentNumber,
    String deliveryNote,
    LocalDateTime createdAt
) {
    public static UserAddressResponse from(UserAddress entity) {
        return new UserAddressResponse(
            entity.getId(),
            entity.getLabel(),
            entity.getCustomLabel(),
            entity.getRecipientName(),
            entity.getRecipientPhone(),
            entity.getFullAddress(),
            entity.getWard(),
            entity.getDistrict(),
            entity.getCity(),
            entity.getIsDefault(),
            entity.getBuilding() != null ? entity.getBuilding().getId() : null,
            entity.getBuilding() != null ? entity.getBuilding().getCode() : null,
            entity.getBuilding() != null ? entity.getBuilding().getName() : null,
            entity.getFloor(),
            entity.getApartmentNumber(),
            entity.getDeliveryNote(),
            entity.getCreatedAt()
        );
    }
}
