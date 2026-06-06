package com.bryan.mapper;

import com.bryan.dto.request.ShippingProviderRequest;
import com.bryan.dto.response.ShippingProviderResponse;
import com.bryan.entity.ShippingProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ShippingProviderMapper {

    ShippingProviderResponse toResponse(ShippingProvider provider);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ShippingProvider toEntity(ShippingProviderRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(ShippingProviderRequest request, @MappingTarget ShippingProvider provider);
}
