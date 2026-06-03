package com.bryan.mapper;

import com.bryan.dto.request.UpdateUserPreferenceRequest;
import com.bryan.dto.response.UserPreferenceResponse;
import com.bryan.entity.UserPreference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "spring")
public interface UserPreferenceMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "bmi", expression = "java(calculateBmi(entity))")
    UserPreferenceResponse toResponse(UserPreference entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "bmi", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateUserPreferenceRequest request, @MappingTarget UserPreference entity);

    default BigDecimal calculateBmi(UserPreference entity) {
        if (entity.getHeightCm() == null || entity.getWeightKg() == null) {
            return null;
        }
        if (entity.getHeightCm().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        BigDecimal heightM = entity.getHeightCm().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return entity.getWeightKg()
                .divide(heightM.multiply(heightM), 2, RoundingMode.HALF_UP);
    }
}
