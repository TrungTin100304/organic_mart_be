package com.bryan.dto.response;

import com.bryan.entity.ResidentialBuilding;

import java.time.LocalDateTime;

public record ResidentialBuildingResponse(
    Long id,
    String code,
    String name,
    String description,
    Integer displayOrder,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ResidentialBuildingResponse from(ResidentialBuilding entity) {
        return new ResidentialBuildingResponse(
            entity.getId(),
            entity.getCode(),
            entity.getName(),
            entity.getDescription(),
            entity.getDisplayOrder(),
            entity.getIsActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
