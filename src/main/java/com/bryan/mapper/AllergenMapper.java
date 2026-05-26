package com.bryan.mapper;

import com.bryan.dto.request.AllergenRequest;
import com.bryan.dto.response.AllergenResponse;
import com.bryan.entity.Allergen;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AllergenMapper {
    AllergenResponse toResponse(Allergen allergen);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Allergen toEntity(AllergenRequest request);
}

