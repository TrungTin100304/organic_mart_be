package com.bryan.mapper;

import com.bryan.dto.response.OrderResponse;
import com.bryan.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "orderCode", target = "orderCode")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userFullName")
    @Mapping(source = "address.id", target = "addressId")
    @Mapping(source = "address.label", target = "addressLabel")
    @Mapping(source = "shippingRecipientSnapshot", target = "shippingRecipientSnapshot")
    @Mapping(source = "shippingPhoneSnapshot", target = "shippingPhoneSnapshot")
    @Mapping(source = "shippingAddressSnapshot", target = "shippingAddressSnapshot")
    @Mapping(source = "subtotal", target = "subtotal")
    @Mapping(source = "discountAmount", target = "discountAmount")
    @Mapping(source = "shippingFee", target = "shippingFee")
    @Mapping(source = "totalAmount", target = "totalAmount")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    // Internal delivery mappings
    @Mapping(source = "deliveryMethod", target = "deliveryMethod")
    @Mapping(source = "deliveryDate", target = "deliveryDate")
    @Mapping(source = "deliverySlotId", target = "deliverySlotId")
    @Mapping(source = "deliverySlotSnapshot", target = "deliverySlotSnapshot")
    @Mapping(source = "buildingCodeSnapshot", target = "buildingCodeSnapshot")
    @Mapping(source = "buildingNameSnapshot", target = "buildingNameSnapshot")
    @Mapping(source = "floorSnapshot", target = "floorSnapshot")
    @Mapping(source = "apartmentNumberSnapshot", target = "apartmentNumberSnapshot")
    @Mapping(source = "recipientNameSnapshot", target = "recipientNameSnapshot")
    @Mapping(source = "recipientPhoneSnapshot", target = "recipientPhoneSnapshot")
    @Mapping(source = "deliveryNoteSnapshot", target = "deliveryNoteSnapshot")
    OrderResponse toResponse(Order order);
}
