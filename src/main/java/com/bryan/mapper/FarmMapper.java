package com.bryan.mapper;

import com.bryan.dto.request.FarmRequest;
import com.bryan.dto.response.FarmResponse;
import com.bryan.entity.Farm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FarmMapper {

    FarmResponse toResponse(Farm farm);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "inventoryBatches", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Farm toEntity(FarmRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "inventoryBatches", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(FarmRequest request, @MappingTarget Farm farm);
}
