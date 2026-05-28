package com.bryan.mapper;

import com.bryan.dto.request.InventoryBatchRequest;
import com.bryan.dto.response.InventoryBatchResponse;
import com.bryan.entity.InventoryBatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InventoryBatchMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "farm.id", target = "farmId")
    @Mapping(source = "farm.name", target = "farmName")
    @Mapping(source = "expired", target = "expired")
    InventoryBatchResponse toResponse(InventoryBatch inventoryBatch);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "farm", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    InventoryBatch toEntity(InventoryBatchRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "farm", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(InventoryBatchRequest request, @MappingTarget InventoryBatch inventoryBatch);
}
