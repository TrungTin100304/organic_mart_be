package com.bryan.mapper;

import com.bryan.dto.request.ProductCategoryRequest;
import com.bryan.dto.response.ProductCategoryResponse;
import com.bryan.entity.ProductCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductCategoryMapper {

    @Mapping(source = "parent.id", target = "parentId")
    ProductCategoryResponse toResponse(ProductCategory category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ProductCategory toEntity(ProductCategoryRequest request);
}

