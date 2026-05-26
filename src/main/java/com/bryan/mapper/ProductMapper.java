package com.bryan.mapper;

import com.bryan.dto.request.ProductRequest;
import com.bryan.dto.response.ProductResponse;
import com.bryan.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProductCategoryMapper.class, AllergenMapper.class})
public interface ProductMapper {

    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "allergens", ignore = true)
    @Mapping(target = "inventoryBatches", ignore = true)
    Product toEntity(ProductRequest request);
}

