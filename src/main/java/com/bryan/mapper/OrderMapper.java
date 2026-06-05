package com.bryan.mapper;

import com.bryan.dto.response.OrderDetailResponse;
import com.bryan.dto.response.OrderResponse;
import com.bryan.dto.response.OrderStatusHistoryResponse;
import com.bryan.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

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
    OrderResponse toResponse(Order order);

    List<OrderDetailResponse> mapDetails(List<OrderDetailResponse> details);

    List<OrderStatusHistoryResponse> mapHistories(List<OrderStatusHistoryResponse> histories);
}
